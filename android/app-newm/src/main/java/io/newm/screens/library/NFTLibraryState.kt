package io.newm.screens.library

import com.slack.circuit.runtime.CircuitUiState
import io.newm.shared.public.models.NFTTrack

sealed interface NFTLibraryState : CircuitUiState {
    data object Loading : NFTLibraryState
    data class LinkWallet(
        val onConnectWallet: (String) -> Unit,
    ) : NFTLibraryState

    data object EmptyWallet : NFTLibraryState
    data class Content(
        val nftTracks: List<NFTTrack>,
        val streamTokenTracks: List<NFTTrack>,
        val showZeroResultFound: Boolean,
        val filters: NFTLibraryFilters,
        val refreshing: Boolean,
        val eventSink: (NFTLibraryEvent) -> Unit,
        val currentTrackId: String?,
        val downloadsEnabled: Boolean
    ) : NFTLibraryState

    data class Error(val message: String) : NFTLibraryState
}

data class NFTLibraryFilters(
    val sortType: NFTLibrarySortType,
    val showShortTracks: Boolean
)

enum class NFTLibrarySortType {
    None,
    ByTitle,
    ByArtist,
    ByLength
}
