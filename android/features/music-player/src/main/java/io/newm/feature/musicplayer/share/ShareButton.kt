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
            contentDescription = "Share Song",
            tint = Color.White
        )
    }
}

fun shareSong(context: Context, songTitle: String, songArtist: String) {
    val randomPhrase = context.getRandomSharePhrase(songTitle, songArtist)
    val callToAction = context.getString(R.string.share_call_to_action)
    val shareText = """
        $randomPhrase
        
        $callToAction
    """.trimIndent()

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    val chooser = Intent.createChooser(shareIntent, "Share song via")
    context.startActivity(chooser)
}