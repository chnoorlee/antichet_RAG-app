package com.antifraud.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antifraud.app.data.HistoryRepository
import com.antifraud.app.data.PreferencesManager
import com.antifraud.app.network.RetrofitClient
import com.antifraud.app.network.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AnalysisUiState {
    object Idle : AnalysisUiState()
    object Loading : AnalysisUiState()
    data class Success(val response: AnalysisResponse) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

sealed class ConnectionUiState {
    object Idle : ConnectionUiState()
    object Checking : ConnectionUiState()
    data class Success(val version: String) : ConnectionUiState()
    data class Error(val message: String) : ConnectionUiState()
}

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    val apiBaseUrl: StateFlow<String> = prefsManager.apiBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreferencesManager.DEFAULT_API_URL)

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionUiState>(ConnectionUiState.Idle)
    val connectionState: StateFlow<ConnectionUiState> = _connectionState.asStateFlow()

    val history = HistoryRepository.history

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun analyze() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) {
            _analysisState.value = AnalysisUiState.Error("请输入要分析的文本")
            return
        }
        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            try {
                val service = RetrofitClient.getService(apiBaseUrl.value)
                val response = service.analyze(AnalyzeRequest(text))
                _analysisState.value = AnalysisUiState.Success(response)
                HistoryRepository.addEntry(
                    AnalysisHistory(
                        inputText = text,
                        riskLevel = response.data.riskLevel,
                        resultType = response.resultType
                    )
                )
            } catch (e: Exception) {
                _analysisState.value = AnalysisUiState.Error(
                    e.localizedMessage ?: "网络请求失败，请检查服务器地址"
                )
            }
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            _connectionState.value = ConnectionUiState.Checking
            try {
                val service = RetrofitClient.getService(apiBaseUrl.value)
                val result = service.health()
                _connectionState.value = ConnectionUiState.Success(result.version)
            } catch (e: Exception) {
                _connectionState.value = ConnectionUiState.Error(
                    e.localizedMessage ?: "无法连接到服务器"
                )
            }
        }
    }

    fun saveApiUrl(url: String) {
        viewModelScope.launch {
            prefsManager.saveApiBaseUrl(url)
            _connectionState.value = ConnectionUiState.Idle
        }
    }

    fun resetAnalysis() {
        _analysisState.value = AnalysisUiState.Idle
    }

    fun clearHistory() {
        HistoryRepository.clear()
    }
}
