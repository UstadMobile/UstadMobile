package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.flowlayout.FlowRow
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntry2DetailBinding
import com.toughra.ustadmobile.databinding.ItemContentJobItemProgressBinding
import com.toughra.ustadmobile.databinding.ItemEntryTranslationBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ContentEntryDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.viewmodel.ContentEntryDetailOverviewUiState
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadQuickActionButton
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface ContentEntryDetailFragmentEventHandler {

    fun handleOnClickOpen()

    fun handleOnClickDownload()

    fun handleOnClickDeleteButton()

    fun handleOnClickManageDownloadButton()

    fun handleOnClickMarkComplete()
}

class ContentEntryDetailOverviewFragment: UstadDetailFragment<ContentEntryWithMostRecentContainer>(
), ContentEntryDetailOverviewView, ContentEntryDetailFragmentEventHandler{

    private var mBinding: FragmentContentEntry2DetailBinding? = null

    private var mPresenter: ContentEntryDetailOverviewPresenter? = null

    private var currentDownloadJobItemStatus: Int = -1

    private var currentLiveData: LiveData<PagedList<ContentEntryRelatedEntryJoinWithLanguage>>? = null

    private var availableTranslationAdapter: AvailableTranslationRecyclerAdapter? = null

    private var progressListAdapter: ContentJobItemProgressRecyclerAdapter? = null

    private val availableTranslationObserver = Observer<List<ContentEntryRelatedEntryJoinWithLanguage>?> {
        t -> run {
            mBinding?.translationVisibility = if (t != null && t.isNotEmpty()) View.VISIBLE else View.GONE
            availableTranslationAdapter?.submitList(t)
        }
    }


    override var entity: ContentEntryWithMostRecentContainer? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntry = value
        }

    override var locallyAvailable: Boolean = false
        set(value) {
            field = value
            mBinding?.locallyAvailable = value
        }

    override var markCompleteVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.markCompleteVisible = value
        }


    override var contentEntryButtons: ContentEntryButtonModel?
        get() = mBinding?.contentEntryButtons
        set(value) {
            if(mBinding?.contentEntryButtons?.showOpenButton != value?.showOpenButton)
                activity?.invalidateOptionsMenu()

            mBinding?.contentEntryButtons = value
        }

    private inner class PresenterViewLifecycleObserver: DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            mPresenter?.onStart()
        }

        override fun onStop(owner: LifecycleOwner) {
            mPresenter?.onStop()
        }
    }

    private var presenterLifecycleObserver: PresenterViewLifecycleObserver? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    override fun handleOnClickOpen() {
        mPresenter?.handleClickOpenButton()
    }

    override fun handleOnClickDownload() {
        mPresenter?.handleClickDownloadButton()
    }

    override fun handleOnClickDeleteButton() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm)
                .setPositiveButton(R.string.delete) { _, _ -> mPresenter?.handleOnClickConfirmDelete() }
                .setNegativeButton(R.string.cancel) { dialog, _ ->  dialog.cancel() }
                .setMessage(R.string.confirm_delete_message)
                .show()
    }

    override fun handleOnClickManageDownloadButton() {
        mPresenter?.handleOnClickManageDownload()
    }

    override fun handleOnClickMarkComplete() {
        mPresenter?.handleOnClickMarkComplete()
    }

    override var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(availableTranslationObserver)
            val accountManager: UstadAccountManager by instance()
            val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)
            val displayTypeRepoVal = dbRepo.contentEntryRelatedEntryJoinDao
            currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
            currentLiveData?.observe(this, availableTranslationObserver)
            field = value
        }


    override fun showDownloadDialog(args: Map<String, String>) {
        val systemImpl: UstadMobileSystemImpl = direct.instance()
        systemImpl.go("DownloadDialog", args, requireContext())
    }

    override var activeContentJobItems: List<ContentJobItemProgress>? = null
        set(value){
            field = value
            mBinding?.contentJobItemProgressList?.visibility = if(value != null && value.isNotEmpty())
                View.VISIBLE else View.GONE
            progressListAdapter?.submitList(value)
        }

    override var scoreProgress: ContentEntryStatementScoreProgress? = null
        get() = field
        set(value) {
            field = value
            mBinding?.scoreProgress = value
        }


    class ContentJobItemProgressRecyclerAdapter():
            ListAdapter<ContentJobItemProgress,
            ContentJobItemProgressRecyclerAdapter.ProgressViewHolder>(DIFF_CALLBACK_CONTENT_JOB_PROGRESS){


        class ProgressViewHolder(val binding: ItemContentJobItemProgressBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
            return ProgressViewHolder(ItemContentJobItemProgressBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
            val item = getItem(position)
            if (item.progressTitle != null) {
                holder.binding.entryDetailProgress.statusText = item.progressTitle.toString()
            }
            holder.binding.entryDetailProgress.progress = if (item.total > 0) {
                (item.progress.toFloat()) / (item.total.toFloat())
            } else {
                0f
            }
        }

    }



    class AvailableTranslationRecyclerAdapter(var activityEventHandler: ContentEntryDetailFragmentEventHandler?,
                                              var presenter: ContentEntryDetailOverviewPresenter?):
            ListAdapter<ContentEntryRelatedEntryJoinWithLanguage, AvailableTranslationRecyclerAdapter.TranslationViewHolder>(DIFF_CALLBACK_ENTRY_LANGUAGE_JOIN) {

        class TranslationViewHolder(val binding: ItemEntryTranslationBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
            val viewHolder = TranslationViewHolder(ItemEntryTranslationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            return viewHolder
        }

        override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
            holder.binding.entryWithLanguage = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
            activityEventHandler = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentContentEntry2DetailBinding.inflate(inflater, container, false).also {
            it.fragmentEventHandler = this
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ContentEntryDetailOverviewScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter = ContentEntryDetailOverviewPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()


        progressListAdapter = ContentJobItemProgressRecyclerAdapter()
        mBinding?.contentJobItemProgressList?.adapter = progressListAdapter
        mBinding?.contentJobItemProgressList?.layoutManager = LinearLayoutManager(requireContext())

        val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        availableTranslationAdapter = AvailableTranslationRecyclerAdapter(this,
                mPresenter)
        mBinding?.availableTranslationView?.adapter = availableTranslationAdapter
        mBinding?.availableTranslationView?.layoutManager = flexboxLayoutManager
        fabManager?.visible = true

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
        presenterLifecycleObserver = PresenterViewLifecycleObserver().also {
            viewLifecycleOwner.lifecycle.addObserver(it)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_content_entry, menu)
        menu.findItem(R.id.content_entry_group_activity).isVisible =
            (contentEntryButtons?.showOpenButton == true)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.content_entry_group_activity -> {
                mPresenter?.handleOnClickGroupActivityButton()
                true
            }
            R.id.action_share -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, mPresenter?.deepLink)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        mBinding = null
        mPresenter = null
        currentLiveData = null
        mBinding?.availableTranslationView?.adapter = null
        mBinding?.contentJobItemProgressList?.adapter = null
        availableTranslationAdapter = null
        availableTranslationsList = null
        progressListAdapter = null
        entity = null
        presenterLifecycleObserver?.also {
            viewLifecycleOwner.lifecycle.removeObserver(it)
        }

        presenterLifecycleObserver = null

        super.onDestroyView()
    }



    companion object{

        val DIFF_CALLBACK_CONTENT_JOB_PROGRESS: DiffUtil.ItemCallback<ContentJobItemProgress> =
                object: DiffUtil.ItemCallback<ContentJobItemProgress>(){
                    override fun areItemsTheSame(
                        oldItem: ContentJobItemProgress,
                        newItem: ContentJobItemProgress
                    ): Boolean {
                      return oldItem.cjiUid == newItem.cjiUid
                    }

                    override fun areContentsTheSame(
                        oldItem: ContentJobItemProgress,
                        newItem: ContentJobItemProgress
                    ): Boolean {
                        return (oldItem.progress == newItem.progress
                                && oldItem.total == newItem.total
                                && oldItem.progressTitle == newItem.progressTitle)
                    }

                }


        val DIFF_CALLBACK_ENTRY_LANGUAGE_JOIN: DiffUtil.ItemCallback<ContentEntryRelatedEntryJoinWithLanguage> =
                object: DiffUtil.ItemCallback<ContentEntryRelatedEntryJoinWithLanguage>() {
            override fun areItemsTheSame(oldItem: ContentEntryRelatedEntryJoinWithLanguage,
                                         newItem: ContentEntryRelatedEntryJoinWithLanguage): Boolean {
                return oldItem.cerejRelatedEntryUid == newItem.cerejRelatedEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentEntryRelatedEntryJoinWithLanguage,
                                            newItem: ContentEntryRelatedEntryJoinWithLanguage): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@Composable
private fun ContentEntryDetailOverviewScreen(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
    onClickDownload: () -> Unit = {},
    onClickOpen: () -> Unit = {},
    onClickMarkComplete: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickManageDownload: () -> Unit = {},
    onClickTranslation: (ContentEntryRelatedEntryJoinWithLanguage) -> Unit = {},
    onClickContentJobItem: () -> Unit = {},
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item {
            ContentDetails(
                uiState = uiState
            )
        }

        if (uiState.contentEntryButtons?.showDownloadButton == true){
            item {
                Button(
                    onClick = onClickDownload,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.download).uppercase())
                }
            }
        }

        if (uiState.contentEntryButtons?.showOpenButton == true){
            item {
                Button(
                    onClick = onClickOpen,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.open).uppercase())
                }
            }
        }

        items(
            items = uiState.activeContentJobItems,
            key = { contentJob -> contentJob.cjiUid }
        ){ contentJobItem ->
            ContentJobListItem(
                uiState = uiState,
                onClickContentJobItem = onClickContentJobItem,
                contentJob = contentJobItem
            )
        }

        if (uiState.locallyAvailable) {
            item {
                LocallyAvailableRow()
            }
        }

        item {
            Divider(thickness = 1.dp)
        }

        item {
            QuickActionBarsRow(
                uiState = uiState,
                onClickMarkComplete = onClickMarkComplete,
                onClickDelete = onClickDelete,
                onClickManageDownload = onClickManageDownload
            )
        }

        item {
            Text(text = uiState.contentEntry?.description ?: "")
        }

        item {
            Divider(thickness = 1.dp)
        }

        if (uiState.translationVisibile){
            item {
                Text(text = stringResource(id = R.string.also_available_in))
            }

            item {
                FlowRow(
                    Modifier.padding(8.dp)
                ) {
                    uiState.availableTranslations.forEach { translation ->
                        TextButton(onClick = { onClickTranslation(translation) }) {
                            Text(text = translation.language?.name ?: "")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContentDetails(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
){
    Row {

        Box(
            modifier = Modifier.weight(0.3F)
        ) {
            LeftColumn(
                uiState = uiState
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier.weight(0.7F)
        ) {
            RightColumn(
                uiState = uiState
            )
        }
    }
}

@Composable
fun LeftColumn(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
){
    Column{
        Image(painter = painterResource(id = R.drawable.book_24px),
            contentDescription = "",
            modifier = Modifier.size(110.dp),
            contentScale = ContentScale.Crop
        )

        BadgedBox(badge = { Badge {
            Icon(
                painter = painterResource(id = R.drawable.ic_content_complete),
                contentDescription = "Favorite"
            )
        } }) {
            if (uiState.scoreProgressVisible){
                LinearProgressIndicator(
                    progress = ((uiState.scoreProgress?.progress ?: 0)/100.0)
                        .toFloat(),
                    modifier = Modifier
                        .height(4.dp)
                )
            }
        }
    }
}

@Composable
fun RightColumn(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
){
    Column(
        modifier = Modifier.height(IntrinsicSize.Max),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {

        Text(
            text = uiState.contentEntry?.title ?: "",
            style = Typography.h4
        )

        if (uiState.authorVisible){
            Text(text = uiState.contentEntry?.author ?: "")
        }

        if (uiState.publisherVisible){
            Text(text = uiState.contentEntry?.publisher ?: "")
        }

        if (uiState.licenseNameVisible){
            Row{

                Text(text = stringResource(id = R.string.entry_details_license))

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = uiState.contentEntry?.licenseName ?: "",
                    style = Typography.h6
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ){

            if (uiState.fileSizeVisible){
                Text(text = UMFileUtil.formatFileSize(
                    uiState.contentEntry?.container?.fileSize ?: 0
                ))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row {
                Image(painter = painterResource(id = R.drawable.ic_baseline_emoji_events_24),
                    contentDescription = "",
                    modifier = Modifier.size(18.dp)
                )

                Text(uiState.scoreProgress?.progress.toString())
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (uiState.scoreResultVisible){
                Text("(" +
                        (uiState.scoreProgress?.resultScore ?: "") +
                        "/" +
                        (uiState.scoreProgress?.resultMax ?: "") +
                        ")"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentJobListItem(
    uiState: ContentEntryDetailOverviewUiState,
    onClickContentJobItem: () -> Unit,
    contentJob: ContentJobItemProgress
){
    ListItem(
        modifier = Modifier.clickable {
            onClickContentJobItem()
        },
        text = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(contentJob.progressTitle ?: "")
                Text(contentJob.progress.toString()+" %")
            }
        },
        secondaryText = {
            LinearProgressIndicator(
                progress = (contentJob.progress/100.0).toFloat(),
                modifier = Modifier
                    .height(4.dp),
            )
        }
    )
}

@Composable
fun LocallyAvailableRow(){
    Row{
        Image(painter = painterResource(id = R.drawable.ic_nearby_black_24px),
            contentDescription = "",
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(text = stringResource(id = R.string.download_locally_availability))
    }
}

@Composable
fun QuickActionBarsRow(
    uiState: ContentEntryDetailOverviewUiState,
    onClickMarkComplete: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickManageDownload: () -> Unit = {}
){
    Row {
        if (uiState.markCompleteVisible){
            UstadQuickActionButton(
                labelText = stringResource(id = R.string.mark_complete).uppercase(),
                imageId = R.drawable.ic_checkbox_multiple_marked,
                onClick = onClickMarkComplete
            )
        }

        if (uiState.contentEntryButtons?.showDeleteButton == true){
            UstadQuickActionButton(
                labelText = stringResource(id = R.string.delete).uppercase(),
                imageId = R.drawable.ic_delete_black_24dp,
                onClick = onClickDelete
            )
        }

        if (uiState.contentEntryButtons?.showManageDownloadButton == true){
            UstadQuickActionButton(
                labelText = stringResource(id = R.string.manage_download).uppercase(),
                imageId = R.drawable.ic_file_download_black_24dp,
                onClick = onClickManageDownload
            )
        }
    }
}

@Composable
@Preview
fun ContentEntryDetailOverviewScreenPreview() {
    val uiStateVal = ContentEntryDetailOverviewUiState(
        contentEntry = ContentEntryWithMostRecentContainer().apply {
            title = "Content Title"
            author = "Author"
            publisher = "Publisher"
            licenseName = "BY_SA"
            container = Container().apply {
                fileSize = 50
            }
            description = "Content Description"
        },
        scoreProgress = ContentEntryStatementScoreProgress().apply {
            /*@FloatRange(from = 0.0, to = 1.0)*/
            progress = 4

            resultScore = 4
            resultMax = 40
        },
        contentEntryButtons = ContentEntryButtonModel().apply {
            showDownloadButton = true
            showOpenButton = true
            showDeleteButton = true
            showManageDownloadButton = true
        },
        availableTranslations = listOf(
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 0
                    name = "Persian"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 1
                    name = "English"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 2
                    name = "Korean"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 3
                    name = "Tamil"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 4
                    name = "Turkish"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 5
                    name = "Telugu"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 6
                    name = "Marathi"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 7
                    name = "Vietnamese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 8
                    name = "Japanese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 9
                    name = "Russian"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 10
                    name = "Portuguese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 11
                    name = "Bengali"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 12
                    name = "Spanish"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 13
                    name = "Hindi"
                }
            }
        ),
        activeContentJobItems = listOf(
            ContentJobItemProgress().apply {
                cjiUid = 0
                progressTitle = "First"
                progress = 30
            },
            ContentJobItemProgress().apply {
                cjiUid = 1
                progressTitle = "Second"
                progress = 10
            },
            ContentJobItemProgress().apply {
                cjiUid = 2
                progressTitle = "Third"
                progress = 70
            }
        ),
        locallyAvailable = true,
        markCompleteVisible = true,
        translationVisibile = true
    )
    MdcTheme {
        ContentEntryDetailOverviewScreen(
            uiState = uiStateVal
        )
    }
}