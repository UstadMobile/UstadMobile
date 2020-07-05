package com.ustadmobile.port.android.view

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentVideoContentBinding
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoContentPresenter
import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.impl.audio.Codec2Player
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.InputStream
import java.io.BufferedInputStream
import java.io.IOException


interface VideoContentFragmentEventHandler {

}

@ExperimentalStdlibApi
class VideoContentFragment : UstadBaseFragment(), VideoPlayerView, VideoContentFragmentEventHandler {


    private var mBinding: FragmentVideoContentBinding? = null

    private var mPresenter: VideoContentPresenter? = null

    private var playerView: PlayerView? = null

    private var player: SimpleExoPlayer? = null

    private var playWhenReady: Boolean = false

    private var currentWindow = 0

    private var playbackPosition: Long = 0

    private var audioPlayer: Codec2Player? = null

    private var subtitleSelection = 1

    private var rootView: View? = null

    private var controlsView: PlayerControlView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentVideoContentBinding.inflate(inflater, container, false).also {
            rootView = it.root
            playerView = it.activityVideoPlayerView
            controlsView = it.playerViewControls
            val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            it.isPortrait = isPortrait
            it.activityVideoPlayerView.useController = !isPortrait
        }

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.get(PLAYBACK) as Long
            playWhenReady = savedInstanceState.get(PLAY_WHEN_READY) as Boolean
            currentWindow = savedInstanceState.get(CURRENT_WINDOW) as Int
        }

        mPresenter = VideoContentPresenter(viewContext,
                arguments.toStringMap(), this, kodein)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(viewLifecycleObserver)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        mBinding?.isPortrait = isPortrait
        mBinding?.activityVideoPlayerView?.useController = !isPortrait
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        playerView = null
        player = null
        audioPlayer = null
        rootView = null
        controlsView = null
    }

    override var entry: ContentEntry? = null
        set(value) {
            field = value
            title = value?.title
            mBinding?.entry = value
        }

    override var videoParams: VideoContentPresenterCommon.VideoParams? = null
        get() = field
        set(value) {
            field = value
            setVideoParams(value?.videoPath, value?.audioPath, value?.srtLangList
                    ?: mutableListOf(), value?.srtMap ?: mutableMapOf())
        }

    override var containerManager: ContainerManager? = null
        get() = field
        set(value) {
            field = value
        }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(viewContext as Context).build()
        player?.addListener(videoListener)
        playerView?.player = player
        controlsView?.player = player
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
    }

    fun setVideoParams(videoPath: String?, audioPath: ContainerEntryWithContainerEntryFile?, srtLangList: MutableList<String>, srtMap: MutableMap<String, String>) {
        if (audioPath != null) {
            player?.addListener(audioListener)
        }

        if (!videoPath.isNullOrEmpty()) {
            val uri = Uri.parse(videoPath)
            val mediaSource = buildMediaSource(uri)

            val subtitles = view?.findViewById<ImageButton>(R.id.exo_subtitle_button)
            if (srtLangList.size > 1) {

                subtitles?.visibility = View.VISIBLE
                val arrayAdapter = ArrayAdapter(viewContext as Context,
                        android.R.layout.select_dialog_singlechoice, srtLangList)

                subtitles?.setOnClickListener {
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
                subtitles?.visibility = View.GONE
                player?.prepare(mediaSource, false, false)
            }
        } else {
            loading = false
        }
    }

    fun setSubtitle(subtitleData: String?, mediaSource: MediaSource) {

        if (subtitleData == null) {
            playerView?.subtitleView?.visibility = View.GONE
            return
        }

        playerView?.subtitleView?.visibility = View.VISIBLE

        val subtitleFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                C.SELECTION_FLAG_DEFAULT, null)

        val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(viewContext)
        val appDatabase = UmAccountManager.getActiveDatabase(viewContext)

        GlobalScope.launch {
            try {
                val containerManager = containerManager
                val containerEntry = containerManager?.getEntry(subtitleData)
                if (containerEntry == null) {
                    showError()
                    loading = false
                    return@launch
                }
                val byteArrayDataSource = ByteArrayDataSource(
                        UMIOUtils.readStreamToByteArray(containerManager.getInputStream(containerEntry)))

                val factory = { byteArrayDataSource }

                val subTitleSource = SingleSampleMediaSource.Factory(factory).createMediaSource(Uri.EMPTY, subtitleFormat, C.TIME_UNSET)

                val mergedSource = MergingMediaSource(mediaSource, subTitleSource)

                runOnUiThread(Runnable {
                    player?.prepare(mergedSource, false, false)
                })
            } catch (ignored: IOException) {
                loading = false
            }
        }
    }

    fun showError() {
        showSnackBar(UstadMobileSystemImpl.instance.getString(MessageID.no_video_file_found, viewContext), {}, 0)
    }

    private var videoListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                loading = false
            }
        }
    }

    private var audioListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            runOnUiThread(Runnable {
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    playbackPosition = player?.contentPosition ?: 0L
                    releaseAudio()
                    playAudio(playbackPosition)
                } else {
                    releaseAudio()
                }
                super.onPlayerStateChanged(playWhenReady, playbackState)
            })

        }
    }


    fun playAudio(fromMs: Long) {
        val audioInput = containerManager?.getInputStream(videoParams?.audioPath
                ?: ContainerEntryWithContainerEntryFile())
        if (audioInput == null) {
            showError()
        }
        audioPlayer = Codec2Player(BufferedInputStream(audioInput), fromMs)
        audioPlayer?.play()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(PLAYBACK, playbackPosition)
        outState.putBoolean(PLAY_WHEN_READY, playWhenReady)
        outState.putInt(CURRENT_WINDOW, currentWindow)
        super.onSaveInstanceState(outState)
    }

    private fun releasePlayer() {
        playbackPosition = player?.currentPosition ?: 0L
        currentWindow = player?.currentWindowIndex ?: 0
        playWhenReady = player?.playWhenReady ?: false
        player?.removeListener(videoListener)
        player?.release()

        player = null
    }

    private fun releaseAudio() {
        audioPlayer?.stop()
    }

    private val viewLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            if (Util.SDK_INT > 23) {
                initializePlayer()
            }
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if (Util.SDK_INT <= 23 || player == null) {
                initializePlayer()
            }
            mPresenter?.onResume()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            if (Util.SDK_INT <= 23) {
                releasePlayer()
                releaseAudio()
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            if (Util.SDK_INT > 23) {
                releasePlayer()
                releaseAudio()
            }

        }

    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(viewContext as Context, "UstadMobile")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
    }

    companion object {

        const val PLAYBACK = "playback"

        const val PLAY_WHEN_READY = "playWhenReady"

        const val CURRENT_WINDOW = "currentWindow"

    }

}