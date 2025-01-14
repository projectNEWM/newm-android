package io.newm.feature.musicplayer.share

import android.content.Context
import android.content.Intent
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.newm.core.resources.R

@Composable
fun ShareButton(
    modifier: Modifier = Modifier,
    songTitle: String? = null,
    songArtist: String? = null,
) {
    if (songTitle.isNullOrBlank() || songArtist.isNullOrBlank()) return
    val context = LocalContext.current
    IconButton(modifier = modifier, onClick = {
        shareSong(context, songTitle, songArtist)
    }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_share),
            contentDescription = stringResource(id = R.string.share_button_icon_description),
            tint = Color.White
        )
    }
}

fun shareSong(context: Context, songTitle: String, songArtist: String) {
    val randomPhrase = context.getRandomSharePhrase(
        songTitle,
        songArtist,
        context.getString(R.string.newm_download_app_landing_page)
    )
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, randomPhrase)
        type = "text/plain"
    }
    val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title))
    context.startActivity(chooser)
}