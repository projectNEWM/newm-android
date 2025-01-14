package io.newm.feature.musicplayer

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.newm.core.theme.White
import io.newm.core.ui.utils.SwipeDirection
import io.newm.core.ui.utils.SwipeableWrapper
import io.newm.feature.musicplayer.models.PlaybackState
import io.newm.feature.musicplayer.models.PlaybackStatus
import io.newm.feature.musicplayer.models.Track
import io.newm.feature.musicplayer.service.MusicPlayer
import io.newm.shared.public.analytics.NewmAppEventLogger
import kotlinx.coroutines.launch
import io.newm.core.resources.R as CoreR


@Composable
fun MiniPlayer(
    eventLogger: NewmAppEventLogger,
    modifier: Modifier = Modifier,
    mediaPlayer: MusicPlayer? = rememberMediaPlayer(eventLogger),
) {
    mediaPlayer ?: return
    val playbackStatus by mediaPlayer.playbackStatus.collectAsState()

    MiniPlayer(
        modifier = modifier,
        onPlayPauseClicked = {
            when (playbackStatus.state) {
                PlaybackState.PLAYING,
                PlaybackState.BUFFERING -> mediaPlayer.pause()

                PlaybackState.PAUSED,
                PlaybackState.STOPPED -> mediaPlayer.play()
            }
        },
        playStatus = playbackStatus,
        onSwipe = { direction ->
            when (direction) {
                SwipeDirection.LEFT -> mediaPlayer.next()  // Swipe right to next song
                SwipeDirection.RIGHT -> mediaPlayer.previous()  // Swipe left to previous song
            }
        },
    )

}


@Composable
fun MiniPlayer(
    onPlayPauseClicked: () -> Unit,
    playStatus: PlaybackStatus,
    onSwipe: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    if (playStatus.track == null) return

    val artistName = playStatus.track.artist
    val songTitle = playStatus.track.title
    val artworkUrl = playStatus.track.artworkUri

    val palette = remember { mutableStateOf<Palette?>(null) }

    val dominantSwatch = remember(palette.value) {
        palette.value?.dominantSwatch
    }

    val dominantColor = remember(dominantSwatch) {
        dominantSwatch?.rgb?.let { Color(it) }
    }

    val titleTextColor = remember(dominantSwatch) {
        dominantSwatch?.titleTextColor?.let { Color(it) }
    }

    val bodyTextColor = remember(dominantSwatch) {
        dominantSwatch?.bodyTextColor?.let { Color(it) }
    }

    val animatedDominantColor by animateColorAsState(
        targetValue = dominantColor ?: MaterialTheme.colors.background,
        label = "animatedDominantColor",
        animationSpec = spring(
            stiffness = StiffnessLow,
        )
    )

    val animatedTitleTextColor by animateColorAsState(
        targetValue = titleTextColor ?: Color.Unspecified,
        label = "animatedTitleTextColor"
    )

    val animatedBodyTextColor by animateColorAsState(
        targetValue = bodyTextColor ?: Color.Unspecified,
        label = "animatedBodyTextColor"
    )

    val coroutineScope = rememberCoroutineScope()


    Card(
        modifier = modifier,
        elevation = 4.dp,
        backgroundColor = animatedDominantColor,
    ) {
        SwipeableWrapper(
            onSwipe = onSwipe
        ) {
            Column {
                MusicPlayerSlider(
                    value = playStatus.elapsedFraction,
                    onValueChange = {},
                    colors = SliderDefaults.colors(
                        thumbColor = White,
                        inactiveTrackColor = Color.DarkGray.copy(alpha = 0.7f)
                    ),
                    allowScrub = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    artworkUrl?.let { url ->
                        val model = remember(url) {
                            ImageRequest.Builder(context)
                                .allowHardware(false)
                                .data(url)
                                .build()
                        }

                        Image(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .clip(shape = RoundedCornerShape(size = 4.dp)),
                            painter = rememberAsyncImagePainter(model = model, onState = {
                                if (it is AsyncImagePainter.State.Success) {
                                    val bitmap = (it.result.drawable as? BitmapDrawable)?.bitmap
                                    bitmap?.let {
                                        coroutineScope.launch {
                                            palette.value = bitmap.getPalletColors()
                                        }
                                    }
                                }
                            }),
                            contentDescription = stringResource(id = CoreR.string.mini_player_artwork_description),
                            contentScale = ContentScale.FillBounds,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column {
                        Text(
                            text = songTitle,
                            style = MaterialTheme.typography.body2,
                            color = animatedTitleTextColor
                        )
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.body2.copy(
                                fontSize = 10.sp,
                            ),
                            color = animatedBodyTextColor
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { onPlayPauseClicked() }) {
                        when (playStatus.state) {
                            PlaybackState.PAUSED,
                            PlaybackState.STOPPED -> {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = stringResource(CoreR.string.mini_player_play_arrow_description),
                                )
                            }

                            PlaybackState.PLAYING,
                            PlaybackState.BUFFERING -> {
                                Icon(
                                    Icons.Default.Pause,
                                    contentDescription = stringResource(CoreR.string.mini_player_pause_description),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMiniPlayer() {
    MiniPlayer(
        playStatus = PlaybackStatus.EMPTY.copy(
            track = Track(
                id = "1",
                title = "Song Title",
                artist = "Artist Name",
                url = ""
            ),
            state = PlaybackState.PLAYING
        ),
        onSwipe = {},
        onPlayPauseClicked = {}
    )
}
