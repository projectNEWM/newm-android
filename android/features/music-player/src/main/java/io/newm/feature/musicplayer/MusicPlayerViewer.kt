package io.newm.feature.musicplayer

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import io.newm.core.resources.R
import io.newm.core.theme.Black
import io.newm.core.theme.DarkPink
import io.newm.core.theme.DarkViolet
import io.newm.core.theme.Gray500
import io.newm.core.theme.GraySuit
import io.newm.core.theme.White
import io.newm.core.theme.inter
import io.newm.core.ui.ZoomableImage
import io.newm.core.ui.utils.SwipeDirection
import io.newm.core.ui.utils.SwipeableWrapper
import io.newm.core.ui.utils.drawWithBrush
import io.newm.core.ui.utils.millisToMinutesSecondsString
import io.newm.feature.musicplayer.models.PlaybackRepeatMode
import io.newm.feature.musicplayer.models.PlaybackState
import io.newm.feature.musicplayer.models.PlaybackStatus
import io.newm.feature.musicplayer.models.Track
import io.newm.feature.musicplayer.share.ShareButton
import io.newm.feature.musicplayer.viewmodel.PlaybackUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val playbackTimeStyle = TextStyle(
    fontSize = 12.sp,
    fontFamily = inter,
    fontWeight = FontWeight.Normal,
    color = GraySuit
)


internal val MusicPlayerBrush = Brush.horizontalGradient(listOf(DarkViolet, DarkPink))

@Composable
internal fun MusicPlayerViewer(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    playbackStatus: PlaybackStatus,
    onEvent: (PlaybackUiEvent) -> Unit,
    onSwipe: (SwipeDirection) -> Unit,
) {
    val song: Track = remember(playbackStatus) { playbackStatus.track } ?: return
    var palette by remember { mutableStateOf<Palette?>(null) }
    val dominantColor = remember(palette) { palette?.dominantColor ?: Black }
    val animatedColor by animateColorAsState(
        dominantColor,
        label = "",
        animationSpec = spring(stiffness = StiffnessLow)
    )

    val context = LocalContext.current
    Box(
        modifier = modifier.background(animatedColor),
    ) {
        val coroutineScope = rememberCoroutineScope()
        SwipeableWrapper(
            modifier = Modifier.align(Alignment.Center),
            onSwipe = onSwipe
        ) {
            ZoomableImage(
                modifier = Modifier.align(Alignment.Center),
                model = ImageRequest.Builder(context)
                    .data(song.artworkUri)
                    .error(R.drawable.ic_default_track_cover_art)
                    .allowHardware(false) // Disable hardware bitmaps.
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                onState = { state ->
                    when (state) {
                        is AsyncImagePainter.State.Success -> {
                            coroutineScope.launch {
                                val drawable = state.result.drawable as? BitmapDrawable
                                drawable?.let {
                                    palette = it.bitmap.getPalletColors()
                                }
                            }
                        }

                        else -> {}
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val buttonModifier =
                    Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))

                IconButton(
                    modifier = buttonModifier,
                    onClick = onNavigateUp
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = stringResource(R.string.back_description),
                        tint = White
                    )
                }

                ShareButton(
                    modifier = buttonModifier,
                    songTitle = playbackStatus.track?.title,
                    songArtist = playbackStatus.track?.artist
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = song.title,
                color = White,
                style = LocalTextStyle.current.copy(
                    fontFamily = inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    lineBreak = LineBreak.Heading,
                    shadow = Shadow(color = Black, blurRadius = 10f, offset = Offset(2f, 3f)),
                )
            )
            Text(
                text = song.artist,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp),
                style = LocalTextStyle.current.copy(
                    color = White,
                    fontFamily = inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    shadow = Shadow(color = Black, blurRadius = 10f, offset = Offset(2f, 0f)),
                )
            )
            MusicPlayerControls(playbackStatus, onEvent)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MusicPlayerControls(
    playbackStatus: PlaybackStatus,
    onEvent: (PlaybackUiEvent) -> Unit
) {
    Box {
        PlaybackControlPanel(
            playbackStatus = playbackStatus,
            onEvent = onEvent
        )
        if (playbackStatus.duration != null) {
            MusicPlayerSlider(
                value = playbackStatus.elapsedFraction,
                onValueChange = { onEvent(PlaybackUiEvent.Seek((it * playbackStatus.duration.inWholeMilliseconds).toLong())) },
                colors = SliderDefaults.colors(
                    thumbColor = White,
                    inactiveTrackColor = Gray500
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(4.dp)

            )
        }
    }
}


@Composable
fun PlaybackControlPanel(
    playbackStatus: PlaybackStatus,
    onEvent: (PlaybackUiEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .height(102.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding()
            .clip(
                shape = RoundedCornerShape(
                    bottomEnd = 8.dp,
                    bottomStart = 8.dp
                )
            )
    ) {
        Column(
            modifier = Modifier
                .background(Black)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = playbackStatus.position.millisToMinutesSecondsString(),
                    style = playbackTimeStyle
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (playbackStatus.duration == null) {
                        "-:--"
                    } else {
                        playbackStatus.duration.inWholeMilliseconds.millisToMinutesSecondsString()
                    },
                    style = playbackTimeStyle
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.padding(12.dp)) {
                RepeatButton(
                    playbackStatus.repeatMode,
                    onClick = { onEvent(PlaybackUiEvent.Repeat) })
                Spacer(modifier = Modifier.weight(1f))
                PreviousTrackButton(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onClick = { onEvent(PlaybackUiEvent.Previous) })
                PlayOrPauseButton(playbackStatus = playbackStatus, onEvent = onEvent)
                NextTrackButton(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onClick = { onEvent(PlaybackUiEvent.Next) })
                Spacer(modifier = Modifier.weight(1f))
                ShuffleButton(
                    enabled = playbackStatus.state == PlaybackState.PLAYING,
                    shuffleMode = playbackStatus.shuffleMode,
                    onClick = { onEvent(PlaybackUiEvent.ToggleShuffle) }
                )
            }
        }
    }
}

@Composable
fun ShuffleButton(
    onClick: () -> Unit,
    shuffleMode: Boolean,
    enabled: Boolean
) {
    IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_music_player_shuffle),
            contentDescription = stringResource(R.string.music_player_shuffle_description),
            tint = when {
                enabled.not() -> Gray500
                shuffleMode -> DarkViolet
                else -> White
            }
        )
    }
}

@Composable
private fun PlayOrPauseButton(
    playbackStatus: PlaybackStatus,
    onEvent: (PlaybackUiEvent) -> Unit
) {
    when (playbackStatus.state) {
        PlaybackState.PLAYING, PlaybackState.BUFFERING -> {
            PauseButton(onClick = {
                onEvent(
                    PlaybackUiEvent.Pause
                )
            })
        }

        PlaybackState.PAUSED, PlaybackState.STOPPED -> {
            PlayButton(onClick = {
                onEvent(
                    PlaybackUiEvent.Play
                )
            })
        }
    }
}

@Composable
private fun PreviousTrackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_prev_track_default),
            contentDescription = stringResource(R.string.music_player_prev_track_description),
            tint = Color.White
        )
    }
}

@Composable
private fun PlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_play),
            contentDescription = stringResource(R.string.music_player_play_description),
            modifier = Modifier.drawWithBrush(MusicPlayerBrush)
        )
    }
}

@Composable
fun PauseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pause),
            contentDescription = stringResource(R.string.music_player_pause_description),
            modifier = Modifier.drawWithBrush(MusicPlayerBrush)
        )
    }
}

@Composable
fun NextTrackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.ic_next_track_default),
            contentDescription = stringResource(R.string.music_player_next_track_description),
            tint = Color.White
        )
    }
}


@Composable
fun RepeatButton(
    repeatMode: PlaybackRepeatMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageRes = when (repeatMode) {
        PlaybackRepeatMode.REPEAT_OFF -> R.drawable.ic_repeat_off
        PlaybackRepeatMode.REPEAT_ONE -> R.drawable.ic_music_player_repeat_one
        PlaybackRepeatMode.REPEAT_ALL -> R.drawable.ic_music_player_repeat_all
    }
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            painter = painterResource(id = imageRes),
            contentDescription = stringResource(R.string.music_player_repeat_description),
            tint = if (repeatMode == PlaybackRepeatMode.REPEAT_OFF) White else DarkViolet
        )
    }
}

val Palette.dominantColor: Color?
    get() {
        val swatch = dominantSwatch ?: vibrantSwatch ?: lightVibrantSwatch ?: darkVibrantSwatch
        return swatch?.let { Color(it.rgb) }
    }

suspend fun Bitmap.getPalletColors(): Palette =
    withContext(Dispatchers.Unconfined) {
        val palette = Palette.from(this@getPalletColors).generate()
        palette
    }

