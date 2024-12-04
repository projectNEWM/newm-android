package io.newm.screens.library

import com.slack.circuit.runtime.CircuitUiEvent
import io.newm.shared.public.models.NFTTrack

sealed interface NFTLibraryEvent : CircuitUiEvent {
    data object OnRefresh : NFTLibraryEvent

    data class PlaySong(val track: NFTTrack) : NFTLibraryEvent

    data class OnQueryChange(val newQuery: String) : NFTLibraryEvent

    data class OnDownloadTrack(val track: NFTTrack) : NFTLibraryEvent

    data class OnApplyFilters(val filters: NFTLibraryFilters) : NFTLibraryEvent
}
