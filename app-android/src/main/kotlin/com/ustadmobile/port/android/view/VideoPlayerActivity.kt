package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.toughra.ustadmobile.R
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoPlayerPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.impl.audio.Codec2Player
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.InputStream
import java.io.BufferedInputStream
import java.io.IOException
import java.util.*


class VideoPlayerActivity : UstadBaseActivity(), VideoPlayerView {
    private lateinit var playerView: PlayerView

    private var player: SimpleExoPlayer? = null

    private var playWhenReady: Boolean = false

    private var currentWindow = 0

    private var playbackPosition: Long = 0

    private lateinit var mPresenter: VideoPlayerPresenter

    internal var isPortrait = true

    private var audioPlayer: Codec2Player? = null

    private var subtitleSelection = 1

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
            playbackPosition = savedInstanceState.get(PLAYBACK) as Long
            playWhenReady = savedInstanceState.get(PLAY_WHEN_READY) as Boolean
            currentWindow = savedInstanceState.get(CURRENT_WINDOW) as Int
        }

        playerView = findViewById(R.id.activity_video_player_view)

        if (isPortrait) {
            setUMToolbar(R.id.activity_video_player_toolbar)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mPresenter = VideoPlayerPresenter(this,
                Objects.requireNonNull(bundleToMap(intent.extras)), this,
                UmAppDatabase.getInstance(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(bundleToMap(savedInstanceState))
    }

    private fun clickUpNavigation() {
        runOnUiThread {
            mPresenter.handleUpNavigation()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
        mPresenter.onResume()
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


    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(this),
                DefaultTrackSelector(), DefaultLoadControl())

        playerView.player = player
        player!!.playWhenReady = playWhenReady
        player!!.seekTo(currentWindow, playbackPosition)
    }

    override fun setVideoParams(videoPath: String?, audioPath: InputStream?, srtLangList: MutableList<String>, srtMap: MutableMap<String, String>) {
        if (audioPath != null) {
            player!!.addListener(audioListener)
        }

        if (!videoPath.isNullOrEmpty()) {
            val uri = Uri.parse(videoPath)
            val mediaSource = buildMediaSource(uri)

            val subtitles = findViewById<ImageButton>(R.id.exo_subtitle_button)
            if (srtLangList.size > 1) {

                subtitles.visibility = VISIBLE
                val arrayAdapter = ArrayAdapter(viewContext as Context,
                        android.R.layout.select_dialog_singlechoice, srtLangList)

                subtitles.setOnClickListener {
                    val builderSingle = AlertDialog.Builder(viewContext as Context)
                    builderSingle.setTitle(R.string.select_subtitle_video)
                    builderSingle.setSingleChoiceItems(arrayAdapter, subtitleSelection) { dialogInterface, position ->
                        subtitleSelection = position
                        val srtName = arrayAdapter.getItem(position)
                        setSubtitle(srtMap[srtName], mediaSource)
                        dialogInterface.cancel()
                    }
                    builderSingle.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    builderSingle.show()

                }
                setSubtitle(srtMap[srtLangList[1]], mediaSource)

            } else {
                subtitles.visibility = GONE

                player?.prepare(mediaSource, false, false)
            }


        }
    }

    fun setSubtitle(subtitleData: String?, mediaSource: MediaSource) {

        if (subtitleData == null) {
            playerView.subtitleView.visibility = GONE
            return
        }

        playerView.subtitleView.visibility = VISIBLE

        val subtitleFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                C.SELECTION_FLAG_DEFAULT, null)

        val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(viewContext)
        val appDatabase = UmAppDatabase.getInstance(viewContext)

        GlobalScope.launch {
            try {
                val containerManager = ContainerManager(mPresenter.container, appDatabase, repoAppDatabase)

                val byteArrayDataSource = ByteArrayDataSource(
                        UMIOUtils.readStreamToString(containerManager.getInputStream(containerManager.getEntry(subtitleData)!!)).toByteArray())

                val factory = { byteArrayDataSource }

                val subTitleSource = SingleSampleMediaSource.Factory(factory).createMediaSource(Uri.EMPTY, subtitleFormat, C.TIME_UNSET)

                val mergedSource = MergingMediaSource(mediaSource, subTitleSource)
                runOnUiThread {
                    player?.prepare(mergedSource, false, false)
                }
            } catch (ignored: IOException) {

            }
        }
    }

    var audioListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            runOnUiThread {
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    playbackPosition = player!!.contentPosition
                    releaseAudio()
                    playAudio(playbackPosition)
                } else {
                    releaseAudio()
                }
                super.onPlayerStateChanged(playWhenReady, playbackState)
            }

        }
    }


    fun playAudio(fromMs: Long) {
        audioPlayer = Codec2Player(BufferedInputStream(mPresenter.audioInput), fromMs)
        audioPlayer!!.play()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(PLAYBACK, playbackPosition)
        outState.putBoolean(PLAY_WHEN_READY, playWhenReady)
        outState.putInt(CURRENT_WINDOW, currentWindow)
        super.onSaveInstanceState(outState)
    }

    private fun releasePlayer() {
        playbackPosition = player?.currentPosition!!
        currentWindow = player?.currentWindowIndex!!
        playWhenReady = player?.playWhenReady!!
        player?.release()
        player = null
    }

    private fun releaseAudio() {
        if (audioPlayer != null) {
            audioPlayer!!.stop()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        if (!isPortrait) {
            playerView.systemUiVisibility = (SYSTEM_UI_FLAG_LOW_PROFILE
                    or SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }

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

    companion object {

        const val PLAYBACK = "playback"

        const val PLAY_WHEN_READY = "playWhenReady"

        const val CURRENT_WINDOW = "currentWindow"


    }
}
