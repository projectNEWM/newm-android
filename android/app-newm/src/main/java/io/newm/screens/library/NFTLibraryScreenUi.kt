@file:OptIn(ExperimentalMaterialApi::class)

package io.newm.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.newm.core.resources.R
import io.newm.core.theme.CerisePink
import io.newm.core.theme.Gray16
import io.newm.core.theme.GraySuit
import io.newm.core.theme.NewmTheme
import io.newm.core.theme.StatusGreen
import io.newm.core.theme.SteelPink
import io.newm.core.theme.White
import io.newm.core.theme.inter
import io.newm.core.theme.raleway
import io.newm.core.ui.LoadingScreen
import io.newm.core.ui.text.SearchBar
import io.newm.core.ui.utils.ErrorScreen
import io.newm.core.ui.utils.drawWithBrush
import io.newm.core.ui.utils.textGradient
import io.newm.screens.library.NFTLibraryEvent.OnApplyFilters
import io.newm.screens.library.NFTLibraryEvent.OnDownloadTrack
import io.newm.screens.library.NFTLibraryEvent.OnQueryChange
import io.newm.screens.library.NFTLibraryEvent.PlaySong
import io.newm.screens.library.screens.EmptyWalletScreen
import io.newm.screens.library.screens.LinkWalletScreen
import io.newm.screens.library.screens.ZeroSearchResults
import io.newm.shared.public.models.NFTTrack
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal const val TAG_NFT_LIBRARY_SCREEN = "TAG_NFT_LIBRARY_SCREEN"


@Composable
fun NFTLibraryScreenUi(
    modifier: Modifier = Modifier,
    state: NFTLibraryState
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag(TAG_NFT_LIBRARY_SCREEN),
    ) {
        when (state) {
            NFTLibraryState.Loading -> LoadingScreen()

            is NFTLibraryState.LinkWallet -> LinkWalletScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) { newmWalletConnectionId ->
                val eventSink = state.onConnectWallet
                eventSink(newmWalletConnectionId)
            }

            NFTLibraryState.EmptyWallet -> EmptyWalletScreen()
            is NFTLibraryState.Error -> ErrorScreen(state.message)
            is NFTLibraryState.Content -> {
                val eventSink = state.eventSink

                Text(
                    text = stringResource(id = R.string.title_nft_library),
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(
                        fontFamily = raleway,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        brush = textGradient(SteelPink, CerisePink)
                    )
                )

                NFTTracks(
                    modifier = Modifier.weight(1f),
                    nftTracks = state.nftTracks,
                    streamTokenTracks = state.streamTokenTracks,
                    showZeroResultsFound = state.showZeroResultFound,
                    filters = state.filters,
                    onQueryChange = { query -> eventSink(OnQueryChange(query)) },
                    onPlaySong = { track -> eventSink(PlaySong(track)) },
                    onDownloadSong = { trackId -> eventSink(OnDownloadTrack(trackId)) },
                    onApplyFilters = { filters -> eventSink(OnApplyFilters(filters)) }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NFTTracks(
    modifier: Modifier = Modifier,
    nftTracks: List<NFTTrack>,
    streamTokenTracks: List<NFTTrack>,
    showZeroResultsFound: Boolean,
    filters: NFTLibraryFilters,
    onQueryChange: (String) -> Unit,
    onPlaySong: (NFTTrack) -> Unit,
    onDownloadSong: (String) -> Unit,
    onApplyFilters: (NFTLibraryFilters) -> Unit
) {
    val scope = rememberCoroutineScope()
    val filterSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .testTag(TAG_NFT_LIBRARY_SCREEN)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        placeholderResId = R.string.library_search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onQueryChange = onQueryChange
                    )
                    IconButton(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 16.dp),
                        onClick = { scope.launch { filterSheetState.show() } }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_library_filter),
                            contentDescription = "Filter",
                            modifier = Modifier.drawWithBrush(LibraryBrush)
                        )
                    }
                }
            }
            when {
                showZeroResultsFound -> item { ZeroSearchResults() }

                nftTracks.isNotEmpty() || streamTokenTracks.isNotEmpty() -> {
                    items(nftTracks + streamTokenTracks, key = { track ->
                        // Use the unique ID as the key
                        track.id
                    }) { track ->
                        Box(
                            modifier = Modifier
                                .background(Gray16)
                        ) {
                            TrackRowItemWrapper(
                                track = track,
                                onPlaySong = onPlaySong,
                                onDownloadSong = { onDownloadSong(track.id) }
                            )
                        }
                    }
                }
            }
        }
        SongFilterBottomSheet(filterSheetState, filters, onApplyFilters)
    }
}

// TODO: Remove this flag once the download functionality is implemented
private const val DOWNLOAD_UI_ENABLED = false

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TrackRowItemWrapper(
    track: NFTTrack,
    onPlaySong: (NFTTrack) -> Unit,
    onDownloadSong: () -> Unit
) {
    val swipeableState = rememberSwipeableState(initialValue = false)
    val deltaX = with(LocalDensity.current) { 82.dp.toPx() }
    Box(
        Modifier
            .height(64.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .swipeable(
                state = swipeableState,
                orientation = Orientation.Horizontal,
                enabled = !track.isDownloaded,
                reverseDirection = true,
                anchors = mapOf(
                    0f to false,
                    deltaX to true,
                ),
            )
    ) {
        if (!track.isDownloaded && DOWNLOAD_UI_ENABLED) {
            RevealedPanel(onDownloadSong)
        }
        TrackRowItem(
            track = track,
            onClick = onPlaySong,
            modifier = if(DOWNLOAD_UI_ENABLED) Modifier.offset {
                IntOffset(
                    x = -swipeableState.offset.value.roundToInt(),
                    y = 0
                )
            } else Modifier
        )
    }
}

@Composable
private fun TrackRowItem(
    track: NFTTrack,
    onClick: (NFTTrack) -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .background(color = Gray16)
            .clickable(onClick = { onClick(track) })
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically

    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.imageUrl)
                .error(R.drawable.ic_default_track_cover_art)
                .placeholder(R.drawable.ic_default_track_cover_art)
                .build(),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
        ) {
            Text(
                text = track.title,
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.isDownloaded) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_downloaded),
                        contentDescription = "Downloaded",
                        tint = StatusGreen
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                }
                Text(
                    text = track.artists.joinToString(","),
                    fontFamily = inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = GraySuit
                )
            }
        }
    }
}

@Preview()
@Composable
fun PreviewNftLibrary() {
    NewmTheme(darkTheme = true) {
        NFTLibraryScreenUi(
            state = NFTLibraryState.Content(
                nftTracks = emptyList(),
                streamTokenTracks = emptyList(),
                showZeroResultFound = false,
                filters = NFTLibraryFilters(
                    sortType = NFTLibrarySortType.None,
                    showShortTracks = false
                ),
                eventSink = {}
            )
        )
    }
}

@Preview
@Composable
fun PreviewNftLibraryLoading() {
    NewmTheme(darkTheme = true) {
        NFTLibraryScreenUi(
            state = NFTLibraryState.Loading
        )
    }
}

@Preview()
@Composable
fun PreviewNftLibraryEmptyWallet() {
    NewmTheme(darkTheme = true) {
        NFTLibraryScreenUi(
            state = NFTLibraryState.EmptyWallet
        )
    }
}
