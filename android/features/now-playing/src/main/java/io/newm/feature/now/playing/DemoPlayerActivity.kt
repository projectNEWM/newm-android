package io.newm.feature.now.playing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class DemoPlayerActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val songId = intent.getStringExtra(EXTRA_SONG_ID)
        if(songId == null) {
            finish()
            return
        }
        setContent {
            DemoPlayerScreen(songId)
        }
    }

    companion object {
        private const val EXTRA_SONG_ID = "song_id"

        fun createIntent(context: Context, songId: String): Intent {
            return Intent(context, DemoPlayerActivity::class.java).apply {
                putExtra(EXTRA_SONG_ID, songId)
            }
        }
    }
}