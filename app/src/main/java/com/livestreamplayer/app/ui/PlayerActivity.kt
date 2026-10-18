package com.livestreamplayer.app.ui

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.livestreamplayer.app.App
import com.livestreamplayer.app.R
import com.livestreamplayer.app.model.FavoriteChannel
import com.livestreamplayer.app.model.WatchHistoryEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var btnBack: ImageButton
    private lateinit var btnPip: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnFullscreen: ImageButton
    private lateinit var channelTitle: TextView
    private lateinit var playerLoading: ProgressBar
    private lateinit var playerError: TextView
    private lateinit var txtQuality: TextView

    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var channelName = ""
    private var channelUrl = ""
    private var channelLogo = ""
    private var channelGroup = ""
    private var isFavorite = false
    private var isFullscreen = false
    private var retryCount = 0
    private val maxRetries = 3

    companion object {
        private const val EXTRA_CHANNEL_NAME = "channel_name"
        private const val EXTRA_CHANNEL_URL = "channel_url"
        private const val EXTRA_CHANNEL_LOGO = "channel_logo"
        private const val EXTRA_CHANNEL_GROUP = "channel_group"

        fun newIntent(
            context: Context, name: String, url: String,
            logo: String = "", group: String = ""
        ): Intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra(EXTRA_CHANNEL_NAME, name)
            putExtra(EXTRA_CHANNEL_URL, url)
            putExtra(EXTRA_CHANNEL_LOGO, logo)
            putExtra(EXTRA_CHANNEL_GROUP, group)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player)

        channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: "Unknown"
        channelUrl = intent.getStringExtra(EXTRA_CHANNEL_URL) ?: ""
        channelLogo = intent.getStringExtra(EXTRA_CHANNEL_LOGO) ?: ""
        channelGroup = intent.getStringExtra(EXTRA_CHANNEL_GROUP) ?: ""

        initViews()
        setupListeners()
        checkFavoriteStatus()
        initializePlayer()
    }

    private fun initViews() {
        playerView = findViewById(R.id.playerView)
        btnBack = findViewById(R.id.btnBack)
        btnPip = findViewById(R.id.btnPip)
        btnShare = findViewById(R.id.btnShare)
        btnFavorite = findViewById(R.id.btnFavoritePlayer)
        btnFullscreen = findViewById(R.id.btnFullscreen)
        channelTitle = findViewById(R.id.channelTitle)
        playerLoading = findViewById(R.id.playerLoading)
        playerError = findViewById(R.id.playerError)
        txtQuality = findViewById(R.id.txtQuality)
        channelTitle.text = channelName
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnPip.setOnClickListener { enterPipMode() }
        btnShare.setOnClickListener {
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Watch $channelName")
                }, "Share"
            ))
        }
        btnFavorite.setOnClickListener { toggleFavorite() }
        btnFullscreen.setOnClickListener { toggleFullscreen() }
        playerError.setOnClickListener { retryPlayback() }
    }

    private fun initializePlayer() {
        playerLoading.visibility = View.VISIBLE
        playerError.visibility = View.GONE

        // Track selector with adaptive quality
        trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters()
                .setMaxVideoSizeSd()
                .setPreferredAudioLanguage("hin")
                .build())
        }

        // Buffer control - larger buffer for live streams
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15000,   // min buffer
                50000,   // max buffer
                2500,    // playback start buffer
                5000     // rebuffer
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // Audio attributes - THIS IS THE KEY FIX for audio
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector!!)
            .setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true)
            .build().also { exoPlayer ->
                // Set audio attributes and enable audio focus
                exoPlayer.setAudioAttributes(audioAttributes, true)

                playerView.player = exoPlayer
                playerView.keepScreenOn = true

                val dataSourceFactory = createDataSourceFactory()
                val mediaSource = buildMediaSource(channelUrl, dataSourceFactory)

                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                exoPlayer.volume = 1.0f  // Ensure volume is max

                // Record watch history
                lifecycleScope.launch {
                    val dao = App.instance.database.watchHistoryDao()
                    val cId = "${channelName}_${channelUrl}".hashCode().toString()
                    if (dao.exists(cId)) {
                        dao.updateWatchTime(cId)
                    } else {
                        dao.addOrUpdate(WatchHistoryEntry(
                            channelId = cId, name = channelName,
                            url = channelUrl, logoUrl = channelLogo, group = channelGroup
                        ))
                    }
                }

                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                playerLoading.visibility = View.VISIBLE
                                playerError.visibility = View.GONE
                            }
                            Player.STATE_READY -> {
                                playerLoading.visibility = View.GONE
                                playerError.visibility = View.GONE
                                retryCount = 0
                                updateQualityText()
                            }
                            Player.STATE_ENDED -> playerLoading.visibility = View.GONE
                            Player.STATE_IDLE -> {}
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        playerLoading.visibility = View.GONE
                        if (retryCount < maxRetries) {
                            retryCount++
                            playerError.visibility = View.VISIBLE
                            playerError.text = "Retrying... ($retryCount/$maxRetries)"
                            lifecycleScope.launch {
                                delay(2000L * retryCount)
                                retryPlayback()
                            }
                        } else {
                            playerError.visibility = View.VISIBLE
                            playerError.text = "Playback failed. Tap to retry.\n${error.message ?: "Unknown error"}"
                        }
                    }
                })
            }
    }

    private fun createDataSourceFactory(): DefaultDataSource.Factory {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
        return DefaultDataSource.Factory(this, httpFactory)
    }

    private fun buildMediaSource(url: String, factory: DefaultDataSource.Factory): MediaSource {
        val uri = Uri.parse(url)
        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.contains(".m3u8") || lowerUrl.contains("/hls") || lowerUrl.contains("/master") || lowerUrl.contains("/playlist") || lowerUrl.contains("/live") || lowerUrl.contains("/index") ->
                HlsMediaSource.Factory(factory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(MediaItem.fromUri(uri))
            lowerUrl.contains(".mpd") || lowerUrl.contains("/dash") ->
                DashMediaSource.Factory(factory)
                    .createMediaSource(MediaItem.fromUri(uri))
            lowerUrl.contains(".ism") || lowerUrl.contains("/smooth") ->
                SsMediaSource.Factory(factory)
                    .createMediaSource(MediaItem.fromUri(uri))
            else ->
                // Default to HLS for most live streams
                HlsMediaSource.Factory(factory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(MediaItem.fromUri(uri))
        }
    }

    private fun retryPlayback() {
        player?.let {
            val dataSourceFactory = createDataSourceFactory()
            val mediaSource = buildMediaSource(channelUrl, dataSourceFactory)
            it.setMediaSource(mediaSource)
            it.prepare()
            it.playWhenReady = true
        }
    }

    private fun updateQualityText() {
        player?.let { p ->
            val format = p.videoFormat
            if (format != null) {
                val quality = when {
                    format.height >= 1080 -> "1080p"
                    format.height >= 720 -> "720p"
                    format.height >= 480 -> "480p"
                    format.height >= 360 -> "360p"
                    else -> "${format.height}p"
                }
                txtQuality.text = quality
            } else {
                txtQuality.text = getString(R.string.auto)
            }
        }
    }

    private fun checkFavoriteStatus() {
        val channelId = "${channelName}_${channelUrl}".hashCode().toString()
        lifecycleScope.launch {
            isFavorite = App.instance.database.favoriteDao().isFavorite(channelId)
            updateFavoriteIcon()
        }
    }

    private fun toggleFavorite() {
        val channelId = "${channelName}_${channelUrl}".hashCode().toString()
        lifecycleScope.launch {
            if (isFavorite) {
                App.instance.database.favoriteDao().removeFavoriteById(channelId)
                isFavorite = false
                Toast.makeText(this@PlayerActivity, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
            } else {
                App.instance.database.favoriteDao().addFavorite(FavoriteChannel(
                    channelId = channelId, name = channelName,
                    url = channelUrl, logoUrl = channelLogo, group = channelGroup
                ))
                isFavorite = true
                Toast.makeText(this@PlayerActivity, R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
            }
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteIcon() {
        btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
        btnFavorite.setColorFilter(getColor(
            if (isFavorite) R.color.favorite_active else android.R.color.white
        ))
    }

    @Suppress("DEPRECATION")
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            findViewById<View>(R.id.playerBottomBar).visibility = View.GONE
            btnBack.visibility = View.GONE
            channelTitle.visibility = View.GONE
            btnPip.visibility = View.GONE
            btnShare.visibility = View.GONE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            findViewById<View>(R.id.playerBottomBar).visibility = View.VISIBLE
            btnBack.visibility = View.VISIBLE
            channelTitle.visibility = View.VISIBLE
            btnPip.visibility = View.VISIBLE
            btnShare.visibility = View.VISIBLE
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
            )
        }
    }

    override fun onPictureInPictureModeChanged(isInPip: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPip, newConfig)
        playerView.useController = !isInPip
        val vis = if (isInPip) View.GONE else View.VISIBLE
        btnBack.visibility = vis
        channelTitle.visibility = vis
        btnPip.visibility = vis
        btnShare.visibility = vis
        findViewById<View>(R.id.playerBottomBar).visibility = vis
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode) {
            player?.playWhenReady = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let { it.playWhenReady = false; it.release() }
        player = null
    }
}
