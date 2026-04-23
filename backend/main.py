"""
反欺诈 RAG 系统 FastAPI 后端
为 Android 客户端提供 REST API
"""

import logging
import os
from contextlib import asynccontextmanager
from typing import List, Optional

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from antifraud_rag import AntiFraudRAG, Settings
from antifraud_rag.db.session import get_session, init_engine

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ---------- Settings ----------
settings = Settings(
    EMBEDDING_MODEL_URL=os.environ["EMBEDDING_MODEL_URL"],
    EMBEDDING_MODEL_API_KEY=os.environ["EMBEDDING_MODEL_API_KEY"],
    EMBEDDING_MODEL_NAME=os.getenv("EMBEDDING_MODEL_NAME", "text-embedding-ada-002"),
    EMBEDDING_DIMENSION=int(os.getenv("EMBEDDING_DIMENSION", "1536")),
    HIGH_RISK_THRESHOLD=float(os.getenv("HIGH_RISK_THRESHOLD", "0.85")),
    DATABASE_URL=os.getenv(
        "DATABASE_URL", "postgresql+asyncpg://user:pass@db:5432/antifraud"
    ),
)


# ---------- Lifespan ----------
@asynccontextmanager
async def lifespan(app: FastAPI):
    init_engine(settings)
    logger.info("Database engine initialized")
    yield


app = FastAPI(
    title="反欺诈 RAG API",
    description="Anti-Fraud RAG System REST API for Android client",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------- Request / Response schemas ----------
class AnalyzeRequest(BaseModel):
    text: str


class AddCaseRequest(BaseModel):
    description: str
    fraud_type: Optional[str] = None
    amount: Optional[float] = None
    keywords: Optional[List[str]] = None


class AddTipRequest(BaseModel):
    title: str
    content: str
    category: Optional[str] = None
    keywords: Optional[List[str]] = None


class SearchRequest(BaseModel):
    query: str
    limit: int = 10


# ---------- Routes ----------
@app.get("/health")
async def health():
    return {"status": "ok", "version": "1.0.0"}


@app.post("/api/v1/analyze")
async def analyze(req: AnalyzeRequest):
    if not req.text or not req.text.strip():
        raise HTTPException(status_code=400, detail="text 不能为空")
    try:
        async with get_session() as db:
            rag = AntiFraudRAG(db, settings=settings)
            result = await rag.analyze(req.text.strip())
            return result
    except Exception as e:
        logger.error(f"analyze error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/v1/cases")
async def add_case(req: AddCaseRequest):
    try:
        async with get_session() as db:
            rag = AntiFraudRAG(db, settings=settings)
            case = await rag.add_case(
                description=req.description,
                fraud_type=req.fraud_type,
                amount=req.amount,
                keywords=req.keywords,
            )
            return {
                "id": str(case.id),
                "description": case.description,
                "fraud_type": case.fraud_type,
            }
    except Exception as e:
        logger.error(f"add_case error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/v1/tips")
async def add_tip(req: AddTipRequest):
    try:
        async with get_session() as db:
            rag = AntiFraudRAG(db, settings=settings)
            tip = await rag.add_tip(
                title=req.title,
                content=req.content,
                category=req.category,
                keywords=req.keywords,
            )
            return {
                "id": str(tip.id),
                "title": tip.title,
                "category": tip.category,
            }
    except Exception as e:
        logger.error(f"add_tip error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/v1/search")
async def hybrid_search(req: SearchRequest):
    try:
        async with get_session() as db:
            rag = AntiFraudRAG(db, settings=settings)
            results = await rag.hybrid_search(req.query, limit=req.limit)
            return {"results": results}
    except Exception as e:
        logger.error(f"hybrid_search error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))
