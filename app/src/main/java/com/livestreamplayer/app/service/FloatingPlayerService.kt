package com.livestreamplayer.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.livestreamplayer.app.App
import com.livestreamplayer.app.R
import com.livestreamplayer.app.ui.PlayerActivity

@UnstableApi
class FloatingPlayerService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var player: ExoPlayer? = null

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_NAME = "extra_name"
        private const val NOTIFICATION_ID = 101

        fun start(context: Context, url: String, name: String) {
            val intent = Intent(context, FloatingPlayerService::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_NAME, name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingPlayerService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val name = intent.getStringExtra(EXTRA_NAME) ?: "Stream"

        startForeground(NOTIFICATION_ID, createNotification(name))
        setupFloatingView(url)

        return START_NOT_STICKY
    }

    private fun createNotification(channelName: String): Notification {
        val stopIntent = Intent(this, FloatingPlayerService::class.java)
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, App.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.floating_player))
            .setContentText(channelName)
            .setSmallIcon(R.drawable.ic_tv)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(R.drawable.ic_close, getString(R.string.stop), stopPendingIntent)
            .build()
    }

    private fun setupFloatingView(url: String) {
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.floating_player, null)

        val layoutParams = WindowManager.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.floating_player_width),
            resources.getDimensionPixelSize(R.dimen.floating_player_height),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // Make it draggable
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        floatingView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, layoutParams)
                    true
                }
                else -> false
            }
        }

        // Close button
        floatingView?.findViewById<View>(R.id.btnCloseFloating)?.setOnClickListener {
            stopSelf()
        }

        windowManager?.addView(floatingView, layoutParams)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        player = ExoPlayer.Builder(this).build().also {
            it.setAudioAttributes(audioAttributes, true)
            it.volume = 1.0f
        }
        val playerView = floatingView?.findViewById<PlayerView>(R.id.floatingPlayerView)
        playerView?.player = player
        playerView?.useController = false

        val dataSourceFactory = DefaultDataSource.Factory(
            this,
            DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)
                .setAllowCrossProtocolRedirects(true)
        )

        val lowerUrl = url.lowercase()
        val mediaSource = if (lowerUrl.contains(".m3u8") || lowerUrl.contains("/hls") || lowerUrl.contains("/master") || lowerUrl.contains("/playlist") || lowerUrl.contains("/live") || lowerUrl.contains("/index")) {
            HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(MediaItem.fromUri(url))
        } else {
            HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(MediaItem.fromUri(url))
        }

        player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        floatingView?.let { windowManager?.removeView(it) }
        floatingView = null
    }
}
