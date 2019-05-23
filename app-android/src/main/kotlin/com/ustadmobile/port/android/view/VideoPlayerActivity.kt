package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.VideoPlayerPresenter
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.impl.audio.Codec2Player

import java.util.Objects

import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import com.ustadmobile.port.android.util.UMAndroidUtil.bundleToMap

class VideoPlayerActivity : UstadBaseActivity(), VideoPlayerView {

    private var playerView: PlayerView? = null

    private var player: SimpleExoPlayer? = null

    private var playWhenReady: Boolean = false

    private var currentWindow = 0

    private var playbackPosition: Long = 0

    private var mPresenter: VideoPlayerPresenter? = null

    internal var isPortrait = true

    private var audioPlayer: Codec2Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isPortrait = true
            setContentView(R.layout.activity_portrait_video_player_view)
        } else {
            isPortrait = false
            setContentView(R.layout.activity_landscape_video_player_view)
        }

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.get("playback") as Long
            playWhenReady = savedInstanceState.get("playWhenReady") as Boolean
            currentWindow = savedInstanceState.get("currentWindow") as Int
        }

        playerView = findViewById(R.id.activity_video_player_view)

        if (isPortrait) {
            setUMToolbar(R.id.activity_video_player_toolbar)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mPresenter = VideoPlayerPresenter(context,
                Objects.requireNonNull(bundleToMap(intent.extras)), this)
        mPresenter!!.onCreate(bundleToMap(savedInstanceState))
    }

    private fun clickUpNavigation() {
        runOnUiThread {
            if (mPresenter != null) {
                mPresenter!!.handleUpNavigation()
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                clickUpNavigation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(this),
                DefaultTrackSelector(), DefaultLoadControl())

        playerView!!.player = player
        if (mPresenter!!.audioPath != null && !mPresenter!!.audioPath!!.isEmpty()) {

            player!!.addListener(object : Player.DefaultEventListener() {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_READY && playWhenReady) {
                        playbackPosition = player!!.contentPosition
                        releaseAudio()
                        playAudio(playbackPosition)
                    } else {
                        releaseAudio()
                    }
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                }
            })
        }

        player!!.playWhenReady = playWhenReady
        player!!.seekTo(currentWindow, playbackPosition)
        setVideoParams(mPresenter!!.videoPath!!, mPresenter!!.audioPath!!, mPresenter!!.srtPath!!)
    }

    override fun setVideoParams(videoPath: String, audioPath: String, srtPath: String) {
        if (audioPath != null && !audioPath.isEmpty()) {

            player!!.addListener(object : Player.DefaultEventListener() {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_READY && playWhenReady) {
                        playbackPosition = player!!.contentPosition
                        releaseAudio()
                        playAudio(playbackPosition)
                    } else {
                        releaseAudio()
                    }
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                }
            })
        }

        if (videoPath != null && !videoPath.isEmpty()) {
            val uri = Uri.parse(videoPath)
            val mediaSource = buildMediaSource(uri)
            var mergedSource: MergingMediaSource? = null

            if (srtPath != null && !srtPath.isEmpty()) {

                val subtitleFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                        C.SELECTION_FLAG_DEFAULT, null)

                val subTitleUri = Uri.parse(srtPath)

                val dataSourceFactory = DefaultDataSourceFactory(this,
                        Util.getUserAgent(this, "com/ustadmobile"))

                val subTitleSource = SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(subTitleUri, subtitleFormat, C.TIME_UNSET)

                mergedSource = MergingMediaSource(mediaSource, subTitleSource)
            }


            player!!.prepare(mergedSource ?: mediaSource, false, false)
        }
    }


    fun playAudio(fromMs: Long) {
        audioPlayer = Codec2Player(mPresenter!!.audioPath!!, fromMs)
        audioPlayer!!.play()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("playback", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)
        outState.putInt("currentWindow", currentWindow)
        super.onSaveInstanceState(outState)
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
            releaseAudio()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
            releaseAudio()
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
    }

    private fun releaseAudio() {
        if (audioPlayer != null) {
            audioPlayer!!.stop()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        if (!isPortrait) {
            playerView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }

    }

    override fun loadUrl(firstUrl: String) {


    }

    override fun setVideoInfo(result: ContentEntry) {
        runOnUiThread {
            if (isPortrait) {
                (findViewById<View>(R.id.activity_video_player_toolbar) as Toolbar).title = result.title
                (findViewById<View>(R.id.activity_video_player_description) as TextView).text = result.description
            }
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(
                FileDataSourceFactory())
                .createMediaSource(uri)
    }
}
