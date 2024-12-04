package io.newm.feature.musicplayer.service

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService

interface DownloadManager {
    fun download(id: String, url: String)
}

class DownloadManagerImpl(
    private val context: Context
) : DownloadManager {
    @UnstableApi
    override fun download(id: String, url: String) {
        val uri = Uri.parse(url)
        val downloadRequest = DownloadRequest.Builder(id, uri).build()

        DownloadService.sendAddDownload(
            context,
            NewmDownloadService::class.java,
            downloadRequest,
            true // isForeground
        )
    }
}