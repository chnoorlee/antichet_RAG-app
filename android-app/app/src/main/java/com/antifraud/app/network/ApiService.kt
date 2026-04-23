package com.antifraud.app.network

import com.antifraud.app.network.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("health")
    suspend fun health(): HealthResponse

    @POST("api/v1/analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): AnalysisResponse

    @POST("api/v1/cases")
    suspend fun addCase(@Body request: AddCaseRequest): Map<String, String>

    @POST("api/v1/tips")
    suspend fun addTip(@Body request: AddTipRequest): Map<String, String>

    @POST("api/v1/search")
    suspend fun search(@Body request: SearchRequest): SearchResponse
}
