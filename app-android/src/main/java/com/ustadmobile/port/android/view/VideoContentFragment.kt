package com.ustadmobile.port.android.view

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentVideoContentBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.VideoContentPresenter
import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.io.ext.openEntryInputStream
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.IOException


interface VideoContentFragmentEventHandler {

}


class VideoContentFragment : UstadBaseFragment(), VideoContentView, VideoContentFragmentEventHandler {

    private var mBinding: FragmentVideoContentBinding? = null

    private var mPresenter: VideoContentPresenter? = null

    private var playerView: PlayerView? = null

    private var player: SimpleExoPlayer? = null

    private var playWhenReady: Boolean = false

    private var currentWindow = 0

    private var playbackPosition: Long = 0

    private var subtitleSelection = 1

    private var rootView: View? = null

    private var controlsView: PlayerControlView? = null

    private var db: UmAppDatabase? = null

    private var containerUid: Long = 0

    private val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentVideoContentBinding.inflate(inflater, container, false).also {
            rootView = it.root
            playerView = it.activityVideoPlayerView
            controlsView = it.playerViewControls
            val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            it.isPortrait = isPortrait
            it.activityVideoPlayerView.useController = !isPortrait
            (context as? MainActivity)?.slideBottomNavigation(isPortrait)
            (context as? MainActivity)?.onAppBarExpand(isPortrait)
            it.videoScroll.isNestedScrollingEnabled = isPortrait
        }

        val accountManager: UstadAccountManager = di.direct.instance()
        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        containerUid = arguments?.getString(UstadView.ARG_CONTAINER_UID)?.toLong() ?: 0L

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.get(PLAYBACK) as Long
            playWhenReady = savedInstanceState.get(PLAY_WHEN_READY) as Boolean
            currentWindow = savedInstanceState.get(CURRENT_WINDOW) as Int
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = VideoContentPresenter(requireContext(),
            arguments.toStringMap(), this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        mBinding?.isPortrait = isPortrait
        mBinding?.activityVideoPlayerView?.useController = !isPortrait
        (context as? MainActivity)?.slideBottomNavigation(isPortrait)
        (context as? MainActivity)?.onAppBarExpand(isPortrait)
        mBinding?.videoScroll?.isNestedScrollingEnabled = isPortrait
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        playerView = null
        player = null
        rootView = null
        controlsView = null
    }

    override var entry: ContentEntry? = null
        set(value) {
            field = value
            ustadFragmentTitle = value?.title
            mBinding?.entry = value
        }

    override var videoParams: VideoContentPresenterCommon.VideoParams? = null
        get() = field
        set(value) {
            field = value
            setVideoParams(value?.videoPath, value?.audioPath, value?.srtLangList
                    ?: mutableListOf(), value?.srtMap ?: mutableMapOf())
        }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        player?.addListener(videoListener)
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        playerView?.player = player
        controlsView?.player = player
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
    }


    fun setVideoParams(videoPath: String?, audioPath: ContainerEntryWithContainerEntryFile?, srtLangList: MutableList<String>, srtMap: MutableMap<String, String>) {
        if (!videoPath.isNullOrEmpty()) {
            val uri = Uri.parse(videoPath)
            val mediaSource = buildMediaSource(uri)

            val subtitles = view?.findViewById<ImageButton>(R.id.exo_subtitle_button)
            if (srtLangList.size > 1) {

                subtitles?.visibility = View.VISIBLE
                val arrayAdapter = ArrayAdapter(requireContext(),
                        android.R.layout.select_dialog_singlechoice, srtLangList)

                subtitles?.setOnClickListener {
                    val builderSingle = AlertDialog.Builder(requireContext())
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
                player?.setMediaSource(mediaSource)
                player?.prepare()
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

        GlobalScope.launch {
            try {
                val subtitleInputStream = db?.containerEntryDao?.openEntryInputStream(containerUid,
                        subtitleData)
                if (subtitleInputStream == null) {
                    showError()
                    loading = false
                    return@launch
                }
                val byteArrayDataSource = ByteArrayDataSource(subtitleInputStream.readBytes())

                val factory = { byteArrayDataSource }

                val subTitleSource = SingleSampleMediaSource.Factory(factory)
                        .createMediaSource(MediaItem.Subtitle(Uri.EMPTY,
                                MimeTypes.APPLICATION_SUBRIP, null), C.TIME_UNSET)

                val mergedSource = MergingMediaSource(mediaSource, subTitleSource)

                runOnUiThread(Runnable {
                    player?.setMediaSource(mergedSource)
                    player?.prepare()
                })
            } catch (ignored: IOException) {
                loading = false
            }
        }
    }

    fun showError() {
        showSnackBar(systemImpl.getString(MessageID.no_video_file_found,
                requireContext()), {}, 0)
    }

    private var videoListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playWhenReady && playbackState == Player.STATE_READY) {
                // media is now playing
                mPresenter?.updateProgress(player?.currentPosition ?: 0, player?.contentDuration
                        ?: 100L, true)
            } else if (playbackState == Player.STATE_ENDED) {
                mPresenter?.updateProgress(player?.currentPosition ?: 0, player?.contentDuration
                        ?: 100L)
            } else if (playbackState == Player.STATE_READY) {
                // player is ready or paused
                loading = false
                mPresenter?.updateProgress(player?.currentPosition ?: 0, player?.contentDuration
                        ?: 100L)
            }
        }


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
        mPresenter?.updateProgress(playbackPosition, player?.contentDuration ?: 100)
        player = null
    }


    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                requireContext(), "UstadMobile")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.Builder().setUri(uri).build())
    }

    companion object {

        const val PLAYBACK = "playback"

        const val PLAY_WHEN_READY = "playWhenReady"

        const val CURRENT_WINDOW = "currentWindow"

    }

}