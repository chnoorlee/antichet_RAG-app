package com.antifraud.app.data

import com.antifraud.app.network.model.AnalysisHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object HistoryRepository {
    private val _history = MutableStateFlow<List<AnalysisHistory>>(emptyList())
    val history: StateFlow<List<AnalysisHistory>> = _history.asStateFlow()

    fun addEntry(entry: AnalysisHistory) {
        val current = _history.value.toMutableList()
        current.add(0, entry)
        if (current.size > 50) current.removeAt(current.size - 1)
        _history.value = current
    }

    fun clear() {
        _history.value = emptyList()
    }
}
