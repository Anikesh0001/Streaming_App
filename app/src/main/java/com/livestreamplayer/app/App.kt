package com.livestreamplayer.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.livestreamplayer.app.data.AppDatabase
import com.livestreamplayer.app.data.PrefsManager

class App : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var prefsManager: PrefsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        prefsManager = PrefsManager(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "playback_channel"
        const val DEFAULT_PLAYLIST_URL = "https://iptv-org.github.io/iptv/countries/in.m3u"
        const val BUNDLED_PLAYLIST_ASSET = "default_playlist.m3u"
        lateinit var instance: App
            private set
    }
}
