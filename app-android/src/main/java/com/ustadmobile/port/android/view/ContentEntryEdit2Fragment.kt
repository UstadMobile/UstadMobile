package com.ustadmobile.port.android.view

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryEdit2Binding
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import kotlinx.android.synthetic.main.fragment_content_entry_edit2.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


interface ContentEntryEdit2FragmentEventHandler {

    fun onClickContentImportSourceSelection()

    fun handleClickLanguage()

    fun handleToggleCompress(checked: Boolean)

}

class ContentEntryEdit2Fragment(private val registry: ActivityResultRegistry? = null) : UstadEditFragment<ContentEntryWithLanguage>(), ContentEntryEdit2View, ContentEntryEdit2FragmentEventHandler {

    private var mBinding: FragmentContentEntryEdit2Binding? = null

    private var mPresenter: ContentEntryEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithLanguage>?
        get() = mPresenter

    private var playerView: PlayerView? = null

    private var player: SimpleExoPlayer? = null

    private var playWhenReady: Boolean = false

    private var currentWindow = 0

    private var playbackPosition: Long = 0

    override var entity: ContentEntryWithLanguage? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntry = value
        }

    override var entryMetaData: ImportedContentEntryMetaData? = null
        get() = field
        set(value) {
            mBinding?.fileImportInfoVisibility = if (value?.uri != null)
                View.VISIBLE else View.GONE
            mBinding?.importedMetadata = value
            field = value
        }

    override var entryCompressed: Boolean = true
        get() = field
        set(value) {
            field = value
            mBinding?.entryCompressed = value
        }

    override var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>? = null
        set(value) {
            field = value
            mBinding?.licenceOptions = value
        }


    override var selectedStorageIndex: Int = 0
        get() = field
        set(value) {
            field = value
            mBinding?.selectedStorageIndex = value
        }


    override var titleErrorEnabled: Boolean = false
        set(value) {
            entry_title.error = getString(R.string.field_required_prompt)
            mBinding?.titleErrorEnabled = value
            field = value
        }

    override var videoFilePath: String? = null
        get() = field
        set(value) {
            mBinding?.showVideoPreview = value != null
            field = value
            if (value != null) {
                prepareVideo(value)
            }
        }

    fun prepareVideo(filePath: String){
        val uri = Uri.parse(filePath)
        if(filePath.startsWith("http")){
            val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer-codelab")
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri)
            player?.prepare(mediaSource)
        }else{
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(requireContext(), "UstadMobile")
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri)
            player?.prepare(mediaSource)
        }
    }

    override var videoDimensions: Pair<Int, Int> = Pair(0,0)
        get(){
            val width = mBinding?.entryEditVideoPreview?.videoSurfaceView?.width ?: 0
            val height = mBinding?.entryEditVideoPreview?.videoSurfaceView?.height ?: 0
            return Pair(width, height)
        }

    override var fileImportErrorVisible: Boolean = false
        set(value) {
            val typedVal = TypedValue()
            requireActivity().theme.resolveAttribute(if (value) R.attr.colorError
            else R.attr.colorOnSurface, typedVal, true)
            mBinding?.fileImportInfoVisibility = if (value) View.VISIBLE else View.GONE
            mBinding?.importErrorColor = typedVal.data
            mBinding?.isImportError = value
            field = value
        }

    override var storageOptions: List<UMStorageDir>? = null
        set(value) {
            mBinding?.storageOptions = value
            field = value
        }


    override var fieldsEnabled: Boolean = false
        set(value) {
            mBinding?.fieldsEnabled = value
            field = value
        }

    override fun onClickContentImportSourceSelection() {
        onSaveStateToBackStackStateHandle()
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setItems(R.array.content_source_option) { dialog, which ->
            when (which) {
                0 -> handleFileSelection()
                1 -> handleLinkSelection()
            }
            dialog.dismiss()
        }
        builder.show()

    }

    override fun handleToggleCompress(checked: Boolean) {
        entryCompressed = checked
    }

    /**
     * removes the temp folder from being deleted in the backstack
     */
    private fun unregisterFileFromTemp() {
        if(entryMetaData?.uri?.startsWith("file://") == true) {
            findNavController().unregisterDestinationTempFile(requireContext(), File(entryMetaData?.uri?.removePrefix("file://")).parentFile)
        }
    }


    internal fun handleFileSelection() {
        registerForActivityResult(ActivityResultContracts.GetContent(),
                registry ?: requireActivity().activityResultRegistry) { uri: Uri? ->
            if (uri != null) {
                try {
                    GlobalScope.launch {
                        val input = requireContext().contentResolver.openInputStream(uri)
                        val importFolder = File(requireContext().filesDir, "import")
                        val importTmpFolder = File(importFolder, "import-${System.currentTimeMillis()}")
                        importTmpFolder.mkdirs()
                        findNavController().registerDestinationTempFile(requireContext(),
                                importTmpFolder)

                        val tmpFile = File(importTmpFolder, requireContext().contentResolver.getFileName(uri))
                        val output = tmpFile.outputStream()
                        input?.copyTo(tmpFile.outputStream())
                        output.flush()
                        output.close()
                        input?.close()

                        mPresenter?.handleFileSelection(tmpFile.path)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }.launch("*/*")
    }

    override fun handleClickLanguage() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Language::class.java, R.id.language_list_dest)
    }

    private fun handleLinkSelection() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(ImportedContentEntryMetaData::class.java, R.id.import_link_view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentContentEntryEdit2Binding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fileImportInfoVisibility = View.GONE
            it.activityEventHandler = this
            it.entryCompressed = true
            it.showVideoPreview = false
            playerView = it.entryEditVideoPreview
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
                viewLifecycleOwner, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
        navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                Language::class.java) {
            val language = it.firstOrNull() ?: return@observeResult
            entity?.language = language
            entity?.primaryLanguageUid = language.langUid
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                ImportedContentEntryMetaData::class.java) {
            val metadata = it.firstOrNull() ?: return@observeResult
            // back from navigate import
            entryMetaData = metadata
            val entry = entryMetaData?.contentEntry
            val entryUid = arguments?.get(ARG_ENTITY_UID)
            if (entry != null) {
                if (entryUid != null) entry.contentEntryUid = entryUid.toString().toLong()
                fileImportErrorVisible = false
                entity = entry
                videoFilePath = metadata.uri
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(viewLifecycleObserver)

    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(viewContext as Context).build()
        playerView?.player = player
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_done){
            if(entity?.let { mPresenter?.isImportValid(it) } == true){
                unregisterFileFromTemp()
            }
        }
        return super.onOptionsItemSelected(item)
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
        entryMetaData = null
        playerView = null
        player = null
    }

    companion object {

        const val PLAYBACK = "playback"

        const val PLAY_WHEN_READY = "playWhenReady"

        const val CURRENT_WINDOW = "currentWindow"

    }

}