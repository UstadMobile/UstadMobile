package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.AdapterView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryEdit2Binding
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.viewmodel.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.LoginUiState
import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ContentEntryAddOptionsBottomSheetFragment.Companion.ARG_SHOW_ADD_FOLDER
import com.ustadmobile.port.android.view.binding.ImageViewLifecycleObserver2
import com.ustadmobile.port.android.view.binding.isSet
import com.ustadmobile.port.android.view.composable.CourseBlockEdit
import com.ustadmobile.port.android.view.composable.UstadExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadTextEditField


interface ContentEntryEdit2FragmentEventHandler {

    fun onClickUpdateContent()

    fun handleClickLanguage()

}

class ContentEntryEdit2Fragment(
) : UstadEditFragment<ContentEntryWithBlockAndLanguage>(), ContentEntryEdit2View,
    ContentEntryEdit2FragmentEventHandler,
    DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>
{

    private var mBinding: FragmentContentEntryEdit2Binding? = null

    private var mPresenter: ContentEntryEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithBlockAndLanguage>?
        get() = mPresenter

    private var playerView: PlayerView? = null

    private var player: SimpleExoPlayer? = null

    private var playWhenReady: Boolean = false

    private var currentWindow = 0

    private var playbackPosition: Long = 0

    private var webView: WebView?  = null

    override var entity: ContentEntryWithBlockAndLanguage? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntry = value
            mBinding?.minScoreVisible = value?.block?.cbCompletionCriteria == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
            mBinding?.gracePeriodVisibility = if(deadlineDate.isSet){
                View.VISIBLE
            }else{
                View.GONE
            }
        }

    override var metadataResult: MetadataResult? = null
        set(value) {
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

    override var contentEntryPicture: ContentEntryPicture?
        get() = mBinding?.contentEntryPicture
        set(value) {
            mBinding?.contentEntryPicture = value
        }


    private var imageViewLifecycleObserver: ImageViewLifecycleObserver2? = null

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
//        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(uri)
//        player?.prepare(mediaSource)
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
    override var caGracePeriodError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caGracePeriodError = value
        }
    override var caDeadlineError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caDeadlineError = value
        }


    override var caStartDateError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caStartDateError = value
        }

    override var caMaxPointsError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caMaxPointsError = value
        }

    override var startDate: Long
        get() = mBinding?.startDate ?: 0
        set(value) {
            mBinding?.startDate = value
        }

    override var startTime: Long
        get() = mBinding?.startTime ?: 0
        set(value) {
            mBinding?.startTime = value
        }

    override var deadlineDate: Long
        get() = mBinding?.deadlineDate ?: Long.MAX_VALUE
        set(value) {
            mBinding?.deadlineDate = value
        }

    override var deadlineTime: Long
        get() = mBinding?.deadlineTime ?: 0
        set(value) {
            mBinding?.deadlineTime = value
        }

    override var gracePeriodDate: Long
        get() = mBinding?.gracePeriodDate ?: Long.MAX_VALUE
        set(value) {
            mBinding?.gracePeriodDate = value
        }

    override var gracePeriodTime: Long
        get() = mBinding?.gracePeriodTime ?: 0
        set(value) {
            mBinding?.gracePeriodTime = value
        }

    override var timeZone: String? = null
        set(value) {
            mBinding?.timeZone = value
            field = value
        }

    override fun onClickUpdateContent() {
        onSaveStateToBackStackStateHandle()
        val entryAddOption = ContentEntryAddOptionsBottomSheetFragment(mPresenter)
        val argsMap = mutableMapOf(ARG_SHOW_ADD_FOLDER to false.toString())
        entryAddOption.arguments = argsMap.toBundle()
        entryAddOption.show(childFragmentManager, entryAddOption.tag)
    }

    override fun handleClickLanguage() {
        mPresenter?.handleClickLanguage()
    }

    var currentDeadlineDate: String? = null

    private var clearDeadlineListener: View.OnClickListener = View.OnClickListener {
        val entityVal = entity
        deadlineDate = Long.MAX_VALUE
        gracePeriodDate = Long.MAX_VALUE
        deadlineTime = 0
        gracePeriodTime = 0
        entityVal?.block?.cbLateSubmissionPenalty = 0
        entity = entityVal
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

            it.entryEditCommonFields.caDeadlineDateTextinput.setEndIconOnClickListener(clearDeadlineListener)
            it.entryEditCommonFields.caDeadlineDate.doAfterTextChanged{ editable ->
                if(editable.isNullOrEmpty()){
                    return@doAfterTextChanged
                }
                if(editable.toString() == currentDeadlineDate){
                    mBinding?.takeIf { bind -> bind.gracePeriodVisibility == View.GONE }.also {
                        mBinding?.gracePeriodVisibility = View.VISIBLE
                    }
                    return@doAfterTextChanged
                }
                mBinding?.gracePeriodVisibility = View.VISIBLE
                currentDeadlineDate = it.toString()
            }

        }

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.get(PLAYBACK) as? Long ?: 0L
            playWhenReady = savedInstanceState.get(PLAY_WHEN_READY) as? Boolean ?: false
            currentWindow = savedInstanceState.get(CURRENT_WINDOW) as? Int ?: 0
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        imageViewLifecycleObserver = ImageViewLifecycleObserver2(
            requireActivity().activityResultRegistry,null, 1).also {
            viewLifecycleOwner.lifecycle.addObserver(it)
            mBinding?.imageViewLifecycleObserver = it
        }

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

@Composable
private fun ContentEntryEditScreen(
    uiState: ContentEntryEditUiState = ContentEntryEditUiState(),
    onCourseBlockChange: (CourseBlock?) -> Unit = {},
    onClickUpdateContent: () -> Unit = {},
    onContentChanged: (ContentEntryWithBlockAndLanguage?) -> Unit = {},
    onChangeCompress: (Boolean) -> Unit = {},
    onChangePubliclyAccessible: (Boolean) -> Unit = {},
    onClickLanguage: () -> Unit = {},
    onSelectContainerStorageDir: (ContainerStorageDir) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    )  {

        val updateContentText =
            if (!uiState.importError.isNullOrBlank())
                stringResource(id = R.string.file_required_prompt)
            else
                stringResource(id = R.string.file_selected)

        if (uiState.updateContentVisible){

            Button(
                onClick = onClickUpdateContent,
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(R.string.update_content).uppercase(),
                    color = contentColorFor(
                        colorResource(id = R.color.secondaryColor))
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(updateContentText)
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.entity?.leaf == true){
            Text(text = stringResource(id = R.string.supported_files))
        }

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.title ?: "",
            label = stringResource(id = R.string.title),
            error = uiState.titleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentChanged(uiState.entity?.shallowCopy {
                        title = it
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.description ?: "",
            label = stringResource(id = R.string.description),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentChanged(uiState.entity?.shallowCopy {
                        description = it
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        CourseBlockEdit(
            uiState = uiState.courseBlockEditUiState,
            onCourseBlockChange = onCourseBlockChange
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.author ?: "",
            label = stringResource(id = R.string.entry_details_author),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentChanged(uiState.entity?.shallowCopy {
                        author = it
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.publisher ?: "",
            label = stringResource(id = R.string.entry_details_publisher),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentChanged(uiState.entity?.shallowCopy {
                        publisher = it
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.entity?.licenseType ?: 0,
            options = LicenceConstants.LICENSE_MESSAGE_IDS,
            label = stringResource(id = R.string.licence),
            enabled = uiState.fieldsEnabled,
            onOptionSelected = {
                onContentChanged(uiState.entity?.shallowCopy {
                        licenseType = it.value
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.containerStorageOptionVisible){
            UstadMessageIdOptionExposedDropDownMenuField(
                value = uiState.entity?.licenseType ?: 0,
                label = "",
                options = PersonConstants.GENDER_MESSAGE_IDS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = {
                    onContentChanged(uiState.entity?.shallowCopy {
                            licenseType = it.value
                        }
                    )
                }
            )

            UstadExposedDropDownMenuField(
                value = uiState.selectedStorageIndex,
                label = stringResource(R.string.content_creation_storage_option_title),
                options = uiState.storageOptions,
                onOptionSelected = { onSelectContainerStorageDir(it as ContainerStorageDir) },
                itemText = { (it as ContainerStorageDir).name ?: "" },
                enabled = uiState.fieldsEnabled,
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.contentCompressVisible){
            SwitchRow(
                text = stringResource(id = R.string.compress),
                checked = uiState.compressionEnabled,
                onChange = onChangeCompress
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        SwitchRow(
            text = stringResource(id = R.string.publicly_accessible),
            checked = uiState.entity?.publik ?: false,
            onChange = onChangePubliclyAccessible
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.language?.name ?: "",
            label = stringResource(id = R.string.language),
            readOnly = true,
            enabled = uiState.fieldsEnabled,
            onClick = onClickLanguage,
            onValueChange = {}
        )
    }
}

@Composable
fun SwitchRow(
    text: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
){
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){

        Text(text)

        Switch(
            checked= checked,
            onCheckedChange = { onChange(it) }
        )
    }
}

@Composable
@Preview
fun ContentEntryEditScreenPreview() {
    val uiStateVal = ContentEntryEditUiState(
        entity = ContentEntryWithBlockAndLanguage().apply {
            leaf = true
        },
        updateContentVisible = true,
        metadataResult = MetadataResult(
            entry = ContentEntryWithLanguage(),
            pluginId = 0
        ),
        courseBlockEditUiState = CourseBlockEditUiState(
            courseBlock = CourseBlock().apply {
                cbMaxPoints = 78
                cbCompletionCriteria = 14
            },
            minScoreVisible = true,
            gracePeriodVisible = true,
        )
    )

    MdcTheme {
        ContentEntryEditScreen(
            uiState = uiStateVal
        )
    }
}