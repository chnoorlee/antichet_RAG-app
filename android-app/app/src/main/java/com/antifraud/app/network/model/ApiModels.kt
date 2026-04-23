package com.antifraud.app.network.model

import com.google.gson.annotations.SerializedName

// ---------- Request ----------
data class AnalyzeRequest(
    @SerializedName("text") val text: String
)

data class AddCaseRequest(
    @SerializedName("description") val description: String,
    @SerializedName("fraud_type") val fraudType: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("keywords") val keywords: List<String>? = null
)

data class AddTipRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("keywords") val keywords: List<String>? = null
)

data class SearchRequest(
    @SerializedName("query") val query: String,
    @SerializedName("limit") val limit: Int = 10
)

// ---------- Response ----------
data class AnalysisResponse(
    @SerializedName("status") val status: String,
    @SerializedName("result_type") val resultType: String,
    @SerializedName("data") val data: AnalysisData
)

data class AnalysisData(
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("matched_cases") val matchedCases: List<MatchedCase>?,
    @SerializedName("recommended_action") val recommendedAction: String?,
    @SerializedName("rrf_score") val rrfScore: Double?,
    @SerializedName("prompt") val prompt: String?,
    @SerializedName("context") val context: RagContext?
)

data class MatchedCase(
    @SerializedName("case_id") val caseId: String,
    @SerializedName("description") val description: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("fraud_type") val fraudType: String?,
    @SerializedName("key_indicators") val keyIndicators: List<String>
)

data class RagContext(
    @SerializedName("relevant_cases") val relevantCases: List<RelevantCase>,
    @SerializedName("anti_fraud_tips") val antiFraudTips: List<AntiFraudTip>
)

data class RelevantCase(
    @SerializedName("description") val description: String,
    @SerializedName("fraud_type") val fraudType: String?
)

data class AntiFraudTip(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: String
)

data class SearchResponse(
    @SerializedName("results") val results: List<SearchResultItem>
)

data class SearchResultItem(
    @SerializedName("case_id") val caseId: String,
    @SerializedName("description") val description: String,
    @SerializedName("fraud_type") val fraudType: String?,
    @SerializedName("rrf_score") val rrfScore: Double
)

// ---------- Local / UI model ----------
data class AnalysisHistory(
    val id: Long = System.currentTimeMillis(),
    val inputText: String,
    val riskLevel: String,
    val resultType: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class RiskLevel(val label: String, val colorHex: Long) {
    HIGH("高风险", 0xFFD32F2F),
    MEDIUM("中风险", 0xFFF57C00),
    LOW("低风险", 0xFF388E3C);

    companion object {
        fun from(value: String): RiskLevel = when (value.uppercase()) {
            "HIGH" -> HIGH
            "MEDIUM" -> MEDIUM
            else -> LOW
        }
    }
}
