package io.newm.feature.musicplayer.service

import android.app.Notification
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import io.newm.feature.musicplayer.R
import org.koin.android.ext.android.inject
import java.util.concurrent.Executor

private const val WORK_NAME: String = "NewmDownload"
private const val FOREGROUND_NOTIFICATION_ID: Int = 1
private const val NOTIFICATION_UPDATE_INTERVAL: Long = 1_000
const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"

@UnstableApi
internal class NewmDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.musicplayer_exo_download_notification_channel_name,
    0
) {
    private val databaseProvider : DatabaseProvider by inject()
    private val downloadCache : Cache by inject()

    // Create a factory for reading the data from the network.
    private val dataSourceFactory = DefaultHttpDataSource.Factory()

    private val downloadExecutor = Executor(Runnable::run)

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = DownloadManager(this, databaseProvider, downloadCache, dataSourceFactory, downloadExecutor)

        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onInitialized(downloadManager: DownloadManager) {
                    super.onInitialized(downloadManager)
                    println("DownloadManager initialized")
                }

                override fun onDownloadsPausedChanged(
                    downloadManager: DownloadManager,
                    downloadsPaused: Boolean
                ) {
                    super.onDownloadsPausedChanged(downloadManager, downloadsPaused)
                    println("Downloads paused: $downloadsPaused")
                }

                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    super.onDownloadChanged(downloadManager, download, finalException)
                    println("Download changed: ${download.request.uri}")
                    println("${download.percentDownloaded}% downloaded")
                }

                override fun onDownloadRemoved(
                    downloadManager: DownloadManager,
                    download: Download
                ) {
                    super.onDownloadRemoved(downloadManager, download)
                    println("Download removed: $download")
                }

                override fun onIdle(downloadManager: DownloadManager) {
                    super.onIdle(downloadManager)
                    println("DownloadManager idle")
                }

                override fun onRequirementsStateChanged(
                    downloadManager: DownloadManager,
                    requirements: Requirements,
                    notMetRequirements: Int
                ) {
                    super.onRequirementsStateChanged(
                        downloadManager,
                        requirements,
                        notMetRequirements
                    )
                    println("Requirements state changed: $requirements")
                }

                override fun onWaitingForRequirementsChanged(
                    downloadManager: DownloadManager,
                    waitingForRequirements: Boolean
                ) {
                    super.onWaitingForRequirementsChanged(downloadManager, waitingForRequirements)
                    println("Waiting for requirements: $waitingForRequirements")
                }
            }
        )
        return downloadManager
    }

    override fun getScheduler(): Scheduler {
        return WorkManagerScheduler(this, WORK_NAME)
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        val downloadNotificationHelper = DownloadNotificationHelper(
            this,
            DOWNLOAD_NOTIFICATION_CHANNEL_ID
        )

        return downloadNotificationHelper.buildProgressNotification(
            this,
            R.drawable.musicplayer_ic_download,
            null,
            null,
            downloads,
            notMetRequirements
        )
    }
}