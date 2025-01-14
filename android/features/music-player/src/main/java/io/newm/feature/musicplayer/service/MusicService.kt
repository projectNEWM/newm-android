package io.newm.feature.musicplayer.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import org.koin.android.ext.android.inject

@UnstableApi
class MediaService : MediaSessionService() {
    private lateinit var mediaSession: MediaSession
    private lateinit var audioManager: AudioManager
    private lateinit var player: ExoPlayer
    private val focusLock = Any()

    private var playbackDelayed = false
    private var playBackAuthorized = false
    private var resumeOnFocusGain = false

    private val downloadCache: Cache by inject()

    companion object {
        private const val DUCKING_VOLUME = 0.2f
        private const val NORMAL_VOLUME = 1.0f
    }

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                restoreVolume()
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    player.play()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                player.stop()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    // only resume if playback is being interrupted
                    resumeOnFocusGain = player.playWhenReady
                    playbackDelayed = false
                }
                player.pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                lowerVolume()
            }
        }
    }


    private val focusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAcceptsDelayedFocusGain(true)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(buildMediaSourceFactory())
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity()
            .setCallback(MediaSessionCallback())
            .build()

    }

    private fun buildMediaSourceFactory(): DefaultMediaSourceFactory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()

        val cacheDataSourceFactory: DataSource.Factory =
            CacheDataSource.Factory()
                .setCache(downloadCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setCacheWriteDataSinkFactory(null) // Disable writing.
        
        return DefaultMediaSourceFactory(this).setDataSourceFactory(cacheDataSourceFactory)
    }

    private fun MediaSession.Builder.setSessionActivity(): MediaSession.Builder {
        val launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName)

        launchIntentForPackage ?: return this

        val pendingIntent = PendingIntent.getActivity(
            this@MediaService,
            21000000,
            launchIntentForPackage,
            PendingIntent.FLAG_IMMUTABLE
        )

        return setSessionActivity(pendingIntent)
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val focusRequestResult = audioManager.requestAudioFocus(focusRequest)

        synchronized(focusLock) {
            playBackAuthorized = when (focusRequestResult) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    player.play()
                    true
                }

                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    playbackDelayed = true
                    false
                }

                else -> false
            }
        }

        return START_NOT_STICKY
    }

    private fun lowerVolume() {
        player.volume = DUCKING_VOLUME
    }

    private fun restoreVolume() {
        player.volume = NORMAL_VOLUME
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        player.release()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands = connectionResult.availableSessionCommands

            return MediaSession.ConnectionResult.accept(
                sessionCommands, connectionResult.availablePlayerCommands
            )
        }
    }
}
