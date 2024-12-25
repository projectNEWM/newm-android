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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.newm.core.resources.R
import io.newm.core.theme.CerisePink
import io.newm.core.theme.DarkPink
import io.newm.core.theme.DarkViolet
import io.newm.core.theme.Gray16
import io.newm.core.theme.GraySuit
import io.newm.core.theme.Purple
import io.newm.core.theme.StatusGreen
import io.newm.core.theme.SteelPink
import io.newm.core.theme.White
import io.newm.core.theme.inter
import io.newm.core.ui.text.SearchBar
import io.newm.core.ui.utils.drawWithBrush
import io.newm.core.ui.utils.secondsToMinutesSecondsString
import io.newm.core.ui.utils.textGradient
import kotlin.math.roundToInt

internal const val TAG_LIBRARY_SCREEN = "TAG_LIBRARY_SCREEN"
internal val LibraryBrush = Brush.horizontalGradient(listOf(DarkViolet, DarkPink))

@Composable
fun LibraryScreen(
    onPlayerClicked: (String) -> Unit,
    onDownloadSong: (String) -> Unit,
    onConnectWallet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TAG_LIBRARY_SCREEN)
            .padding(horizontal = 16.dp)
            .verticalScroll(state = rememberScrollState())
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 19.dp),
            text = stringResource(id = R.string.title_library),
            style = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                brush = textGradient(SteelPink, CerisePink)
            )
        )
        if (savedSongModels.isEmpty()) {
            EmptyLibraryContent(
                modifier = Modifier.weight(1f),
                onConnectWallet = onConnectWallet
            )
        } else {
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
                    onQueryChange = { /*TODO*/ }
                )

                IconButton(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp, start = 16.dp),
                    onClick = {/*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_library_filter),
                        contentDescription = stringResource(R.string.filter_description),
                        modifier = Modifier
                            .drawWithBrush(LibraryBrush)

                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray16)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                ) {
                    for (model in savedSongModels) {
                        LibraryItem(
                            songModel = model,
                            onPlaySong = { onPlayerClicked(model.id) },
                            onDownloadSong = { onDownloadSong(model.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LibraryItem(songModel: SongModel, onPlaySong: () -> Unit, onDownloadSong: () -> Unit) {
    val swipeableState = rememberSwipeableState(initialValue = false)
    val deltaX = with(LocalDensity.current) { 82.dp.toPx() }
    Box(
        Modifier
            .height(56.dp)
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .swipeable(
                state = swipeableState,
                orientation = Orientation.Horizontal,
                enabled = !songModel.isDownloaded,
                reverseDirection = true,
                anchors = mapOf(
                    0f to false,
                    deltaX to true,
                )
            )
    ) {
        if (!songModel.isDownloaded) {
            RevealedPanel(onDownloadSong)
        }
        LibraryCard(
            songModel = songModel,
            onClick = onPlaySong,
            modifier = Modifier.offset {
                IntOffset(
                    x = -swipeableState.offset.value.roundToInt(),
                    y = 0
                )
            }
        )
    }
}

@Composable
fun RevealedPanel(onDownloadClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Purple)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onDownloadClick, modifier = Modifier.size(16.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_download),
                    contentDescription = stringResource(R.string.library_download_description)
                )
            }
            Text(
                text = stringResource(id = R.string.library_download),
                style = TextStyle(
                    fontFamily = inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = White
                )
            )
        }

    }
}

@Composable
private fun LibraryCard(
    songModel: SongModel,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(color = Gray16)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = songModel.imageUrl,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
        ) {
            Text(
                text = songModel.title,
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (songModel.isDownloaded) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_downloaded),
                        contentDescription = stringResource(R.string.downloaded_description),
                        tint = StatusGreen
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                }
                Text(
                    text = songModel.artist,
                    fontFamily = inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = GraySuit
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = songModel.duration.secondsToMinutesSecondsString(),
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = GraySuit
        )
    }
}
