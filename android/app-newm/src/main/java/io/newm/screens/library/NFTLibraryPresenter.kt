package io.newm.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import io.newm.feature.musicplayer.models.PlaybackState
import io.newm.feature.musicplayer.models.Playlist
import io.newm.feature.musicplayer.models.Track
import io.newm.feature.musicplayer.rememberMediaPlayer
import io.newm.feature.musicplayer.service.DownloadManager
import io.newm.feature.musicplayer.service.MusicPlayer
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.analytics.events.AppScreens
import io.newm.shared.public.featureflags.FeatureFlagManager
import io.newm.shared.public.featureflags.FeatureFlags
import io.newm.shared.public.models.NFTTrack
import io.newm.shared.public.usecases.ConnectWalletUseCase
import io.newm.shared.public.usecases.HasWalletConnectionsUseCase
import io.newm.shared.public.usecases.SyncWalletConnectionsUseCase
import io.newm.shared.public.usecases.WalletNFTTracksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class NFTLibraryPresenter(
    private val navigator: Navigator,
    private val hasWalletConnectionsUseCase: HasWalletConnectionsUseCase,
    private val connectWalletUseCase: ConnectWalletUseCase,
    private val syncWalletConnectionsUseCase: SyncWalletConnectionsUseCase,
    private val walletNFTTracksUseCase: WalletNFTTracksUseCase,
    private val scope: CoroutineScope,
    private val eventLogger: NewmAppEventLogger,
    private val downloadManager: DownloadManager,
    private val featureFlagManager: FeatureFlagManager,
) : Presenter<NFTLibraryState> {
    @Composable
    override fun present(): NFTLibraryState {
        val musicPlayer: MusicPlayer? = rememberMediaPlayer(eventLogger)

        val downloadsEnabled =
            remember { featureFlagManager.isEnabled(FeatureFlags.DownloadTracks) }

        LaunchedEffect(Unit) {
            syncWalletConnectionsUseCase.syncWalletConnectionsFromNetworkToDevice()
        }

        val isWalletConnected: Boolean? by remember { hasWalletConnectionsUseCase.hasWalletConnectionsFlow() }.collectAsRetainedState(
            null
        )

        val isWalletSynced by remember { walletNFTTracksUseCase.walletSynced }.collectAsState(
            false
        )

        var query by rememberSaveable { mutableStateOf("") }

        val nftTracks by remember(isWalletConnected) {
            if (isWalletConnected == true) {
                walletNFTTracksUseCase.getAllCollectableTracksFlow()
            } else {
                flowOf()
            }
        }.collectAsRetainedState(initial = emptyList())

        // Do not show stream tokens in the library
        val streamTracks = emptyList<NFTTrack>()

        var filters: NFTLibraryFilters by rememberRetained {
            mutableStateOf(
                NFTLibraryFilters(
                    sortType = NFTLibrarySortType.None,
                    showShortTracks = false
                )
            )
        }

        val filteredNftTracks = remember(nftTracks, query, filters) {
            nftTracks.filterAndSort(query, filters)
        }

        val filteredStreamTokens = remember(streamTracks, query, filters) {
            streamTracks.filterAndSort(query, filters)
        }

        val playList = remember(
            filteredNftTracks,
            filteredStreamTokens
        ) { Playlist(filteredNftTracks.toTrack() + filteredStreamTokens.toTrack()) }

        val currentTrackId = musicPlayer?.let {
            val playbackStatus by musicPlayer.playbackStatus.collectAsState()
            remember(playbackStatus) { playbackStatus.track?.id.takeIf { playbackStatus.state != PlaybackState.BUFFERING } }
        }

        val showZeroResultFound = remember(query, nftTracks, streamTracks) {
            (query.isNotEmpty()
                    && nftTracks.none { it.matches(query) }
                    && streamTracks.none { it.matches(query) })
        }

        val isLoading = remember(isWalletConnected, isWalletSynced, playList.tracks) {
            when (isWalletConnected) {
                // if playlist is empty and wallet is not synced, show loading
                true -> playList.tracks.isEmpty() && isWalletSynced.not()
                // if wallet is not connected, don't show loading
                false -> false
                // Wallet connection state is unknown, show loading
                null -> true
            }
        }

        val isWalletEmpty = isWalletSynced && playList.tracks.isEmpty() && !showZeroResultFound

        var refreshing by remember { mutableStateOf(false) }

        fun refresh() = scope.launch {
            refreshing = true
            walletNFTTracksUseCase.refresh()
            refreshing = false
        }

        if (isWalletConnected == true) {
            LaunchedEffect(Unit) {
                refresh()
            }
        }

        return when {
            isLoading -> NFTLibraryState.Loading
            isWalletConnected == false -> NFTLibraryState.LinkWallet { newmWalletConnectionId ->
                scope.launch {
                    connectWalletUseCase.connect(newmWalletConnectionId)
                }
            }

            isWalletEmpty -> NFTLibraryState.EmptyWallet
            else -> {
                NFTLibraryState.Content(
                    nftTracks = filteredNftTracks,
                    streamTokenTracks = filteredStreamTokens,
                    showZeroResultFound = showZeroResultFound,
                    filters = filters,
                    refreshing = refreshing,
                    eventSink = { event ->
                        when (event) {
                            is NFTLibraryEvent.OnDownloadTrack -> {
                                downloadManager.download(
                                    id = event.track.id,
                                    url = event.track.audioUrl
                                )
                            }

                            is NFTLibraryEvent.OnQueryChange -> {
                                eventLogger.logEvent(
                                    AppScreens.NFTLibraryScreen.SEARCH_BUTTON,
                                    mapOf("query" to event.newQuery)
                                )
                                query = event.newQuery
                            }

                            is NFTLibraryEvent.PlaySong -> {
                                val trackIndex =
                                    playList.tracks.indexOfFirst { it.id == event.track.id }

                                require(trackIndex >= 0) { "Track not found in playlist" }

                                musicPlayer?.apply {
                                    setPlaylist(playList, trackIndex)
                                    play()
                                }
                            }

                            is NFTLibraryEvent.OnApplyFilters -> {
                                eventLogger.logClickEvent(AppScreens.NFTLibraryFilterScreen.APPLY_BUTTON)
                                filters = event.filters
                            }

                            NFTLibraryEvent.OnRefresh -> {
                                eventLogger.logClickEvent(AppScreens.NFTLibraryScreen.REFRESH_BUTTON)
                                refresh()
                            }
                        }
                    },
                    currentTrackId = currentTrackId,
                    downloadsEnabled = downloadsEnabled,
                )
            }
        }
    }

    private fun List<NFTTrack>.filterAndSort(
        query: String,
        filters: NFTLibraryFilters
    ): List<NFTTrack> {
        val filteredTracks = filter {
            it.matches(query) && (filters.showShortTracks || it.duration >= 30)
        }
        return when (filters.sortType) {
            NFTLibrarySortType.None -> filteredTracks
            NFTLibrarySortType.ByTitle -> filteredTracks.sortedBy { it.title }
            NFTLibrarySortType.ByArtist -> filteredTracks.sortedBy { it.artists.first() }
            NFTLibrarySortType.ByLength -> filteredTracks.sortedBy { it.duration }
        }
    }
}

private fun NFTTrack.matches(query: String): Boolean {
    if (query.isBlank()) return true
    return (artists + title).any { it.contains(query, ignoreCase = true) }
}


private fun List<NFTTrack>.toTrack(): List<Track> = map { nftTrack ->
    Track(
        id = nftTrack.id,
        title = nftTrack.title,
        url = nftTrack.audioUrl,
        artist = nftTrack.artists.firstOrNull() ?: "",
        artworkUri = nftTrack.imageUrl,
    )
}
