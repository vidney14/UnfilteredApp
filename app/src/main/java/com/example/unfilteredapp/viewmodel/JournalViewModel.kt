package com.example.unfilteredapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unfilteredapp.data.model.JournalEntryResponse
import com.example.unfilteredapp.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class JournalState {
    object Idle : JournalState()
    object Loading : JournalState()
    data class Success(val entries: List<JournalEntryResponse>) : JournalState()
    data class Error(val message: String) : JournalState()
}

class JournalViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    private val _state = MutableStateFlow<JournalState>(JournalState.Idle)
    val state: StateFlow<JournalState>
        get() = _state

    fun fetchEntries() {
        viewModelScope.launch {
            _state.value = JournalState.Loading

            runCatching {
                repository.getEntries()
            }.onSuccess { response ->
                val entries = response.body()

                _state.value = if (response.isSuccessful && entries != null) {
                    JournalState.Success(entries)
                } else {
                    JournalState.Error("Failed to fetch entries")
                }
            }.onFailure { error ->
                _state.value = JournalState.Error(
                    error.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    fun addEntry(content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                repository.saveEntry(trimmedContent)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    fetchEntries()
                }
            }
        }
    }
}
