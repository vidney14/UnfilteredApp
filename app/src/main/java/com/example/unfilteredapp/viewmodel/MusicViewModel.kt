package com.example.unfilteredapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unfilteredapp.data.model.SpotifyTrack
import com.example.unfilteredapp.data.repository.SpotifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MusicState {
    object Idle : MusicState()
    object Loading : MusicState()
    data class Success(val tracks: List<SpotifyTrack>) : MusicState()
    data class Error(val message: String) : MusicState()
}

class MusicViewModel(
    private val repository: SpotifyRepository
) : ViewModel() {

    private val _musicState = MutableStateFlow<MusicState>(MusicState.Idle)
    val musicState: StateFlow<MusicState>
        get() = _musicState

    fun loadMoodSuggestions(mood: String) {
        if (mood.isBlank()) {
            _musicState.value = MusicState.Error("Please enter a mood")
            return
        }

        viewModelScope.launch {
            _musicState.value = MusicState.Loading

            runCatching {
                repository.getMoodSuggestions(mood.trim())
            }.onSuccess { tracks ->
                _musicState.value = if (tracks.isNotEmpty()) {
                    MusicState.Success(tracks)
                } else {
                    MusicState.Error("No suggestions found for this mood")
                }
            }.onFailure { exception ->
                _musicState.value = MusicState.Error(
                    exception.message ?: "Unknown error occurred"
                )
            }
        }
    }
}
