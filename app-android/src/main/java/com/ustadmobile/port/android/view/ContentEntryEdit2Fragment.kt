package com.ustadmobile.port.android.view

import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.AdapterView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryEdit2Binding
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ContentEntryAddOptionsBottomSheetFragment.Companion.ARG_SHOW_ADD_FOLDER
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList


interface ContentEntryEdit2FragmentEventHandler {

    fun onClickUpdateContent()

    fun handleClickLanguage()

}

class ContentEntryEdit2Fragment(
) : UstadEditFragment<ContentEntryWithLanguage>(), ContentEntryEdit2View,
    ContentEntryEdit2FragmentEventHandler,
    DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>
{

    private var mBinding: FragmentContentEntryEdit2Binding? = null

    private var mPresenter: ContentEntryEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithLanguage>?
        get() = mPresenter

    private var playerView: PlayerView? = null

    private var player: SimpleExoPlayer? = null

    private var playWhenReady: Boolean = false

    private var currentWindow = 0

    private var playbackPosition: Long = 0

    private var webView: WebView?  = null

    override var entity: ContentEntryWithLanguage? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntry = value
            mBinding?.minScoreVisible = value?.completionCriteria == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
        }

    override var entryMetaData: ImportedContentEntryMetaData? = null
        get() = field
        set(value) {
            field = value
        }

    override var metadataResult: MetadataResult? = null
        set(value) {
            // TODO need to remove fileSelectedInfo if metadata came from scraper
            mBinding?.metadataResult = value
            field = value
        }

    override var compressionEnabled: Boolean = true
        get() = mBinding?.compressionEnabled ?: true
        set(value) {
            field = value
            mBinding?.compressionEnabled = value
        }

    override var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>? = null
        set(value) {
            field = value
            mBinding?.licenceOptions = value
        }

    override var showCompletionCriteria: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.showCompletionCriteria = value
        }

    override var completionCriteriaOptions: List<ContentEntryEdit2Presenter.CompletionCriteriaMessageIdOption>? = null
        set(value) {
            field = value
            mBinding?.completionCriteriaOptions = value
        }


    override var selectedStorageIndex: Int = 0
        get() = field
        set(value) {
            field = value
            mBinding?.selectedStorageIndex = value
        }


    override var titleErrorEnabled: Boolean = false
        set(value) {
            mBinding?.entryTitle?.error = getString(R.string.field_required_prompt)
            mBinding?.titleErrorEnabled = value
            field = value
        }

    override var videoUri: String? = null
        get() = field
        set(value) {

            field = value
            if(value == null) return
            if (value.startsWith("http")) {
                mBinding?.showVideoPreview = false
                mBinding?.showWebPreview = true
                prepareVideoFromWeb(value)
            }else{
                mBinding?.showVideoPreview = true
                mBinding?.showWebPreview = false
                prepareVideoFromFile(value)
            }
        }

    private fun prepareVideoFromFile(filePath: String) {
        val uri = Uri.parse(filePath)
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(requireContext(), "UstadMobile")
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        player?.prepare(mediaSource)
    }

    private fun prepareVideoFromWeb(filePath: String){
        webView?.loadData("""
            <!DOCTYPE html>
            <html lang="en">
            <body>
            
             <video id="video" style="width: 100%;height: 175px" controls>
                <source src="$filePath"
             </video> 
            
            </body>
            </html>
        """.trimIndent(), "text/html", "UTF-8")
    }

    override val videoDimensions: Pair<Int, Int>
        get() {
            val width = mBinding?.entryEditVideoPreview?.videoSurfaceView?.width ?: 0
            val height = mBinding?.entryEditVideoPreview?.videoSurfaceView?.height ?: 0
            return Pair(width, height)
        }

    override var fileImportErrorVisible: Boolean = false
        set(value) {
            val typedVal = TypedValue()
            requireActivity().theme.resolveAttribute(if (value) R.attr.colorError
            else R.attr.colorOnSurface, typedVal, true)
            mBinding?.importErrorColor = typedVal.data
            mBinding?.isImportError = value
            field = value
        }

    override var storageOptions: List<ContainerStorageDir>? = null
        set(value) {
            mBinding?.storageOptions = value
            field = value
        }


    override var fieldsEnabled: Boolean = false
        set(value) {
            super.fieldsEnabled = value
            mBinding?.fieldsEnabled = value
            field = value
        }

    override var showUpdateContentButton: Boolean
        get() = mBinding?.showUpdateContentButton ?: false
        set(value) {
            mBinding?.showUpdateContentButton = value
        }

    override fun onClickUpdateContent() {
        onSaveStateToBackStackStateHandle()
        val entryAddOption = ContentEntryAddOptionsBottomSheetFragment(mPresenter)
        val argsMap = mutableMapOf(ARG_SHOW_ADD_FOLDER to false.toString())
        entryAddOption.arguments = argsMap.toBundle()
        entryAddOption.show(childFragmentManager, entryAddOption.tag)
    }

    override fun handleClickLanguage() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Language::class.java, R.id.language_list_dest)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentContentEntryEdit2Binding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            it.compressionEnabled = true
            it.showVideoPreview = false
            it.showWebPreview = false
            it.completionCriteriaListener = this
            webView = it.entryEditWebPreview
            webView?.webChromeClient = WebChromeClient()
            playerView = it.entryEditVideoPreview
            webView?.settings?.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                mediaPlaybackRequiresUserGesture = true
            }
        }

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.get(PLAYBACK) as Long
            playWhenReady = savedInstanceState.get(PLAY_WHEN_READY) as Boolean
            currentWindow = savedInstanceState.get(CURRENT_WINDOW) as Int
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        ustadFragmentTitle = getString(R.string.content)

        mPresenter = ContentEntryEdit2Presenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                Language::class.java) {
            val language = it.firstOrNull() ?: return@observeResult
            entity?.language = language
            entity?.primaryLanguageUid = language.langUid
        }

        viewLifecycleOwner.lifecycle.addObserver(viewLifecycleObserver)

    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView?.player = player
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mBinding?.minScoreVisible = selectedOption.optionId == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

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
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            if (Util.SDK_INT <= 23) {
                releasePlayer()
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            if (Util.SDK_INT > 23) {
                releasePlayer()
            }

        }

    }

    private fun releasePlayer() {
        playbackPosition = player?.currentPosition ?: 0L
        currentWindow = player?.currentWindowIndex ?: 0
        playWhenReady = player?.playWhenReady ?: false
        player?.release()
        player = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        metadataResult = null
        playerView = null
        webView = null
        player = null
    }

    companion object {

        const val PLAYBACK = "playback"

        const val PLAY_WHEN_READY = "playWhenReady"

        const val CURRENT_WINDOW = "currentWindow"

    }

}