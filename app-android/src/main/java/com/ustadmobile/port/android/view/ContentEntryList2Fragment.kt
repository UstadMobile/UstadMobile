package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.locale.entityconstants.ContentEntryTypeLabelConstants
import com.ustadmobile.core.util.ext.determineListMode
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SELECT_FOLDER_VISIBLE
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_TITLE
import com.ustadmobile.core.viewmodel.ContentEntryListUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_SUCCESS
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_UNSET
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.ContentEntryAddOptionsBottomSheetFragment.Companion.ARG_SHOW_ADD_FOLDER
import com.ustadmobile.port.android.view.ContentEntryDetailOverviewFragment.Companion.CONTENT_ENTRY_TYPE_ICON_MAP
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import org.kodein.di.direct
import org.kodein.di.instance

class ContentEntryList2Fragment : UstadListViewFragment<ContentEntry, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(),
        ContentEntryList2View, View.OnClickListener, FragmentBackHandler{

    private val systemImpl: UstadMobileSystemImpl by instance()

    private var mPresenter: ContentEntryList2Presenter? = null

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter


    override fun onHostBackPressed() = mPresenter?.handleOnBackPressed() ?: false

    override fun showDownloadDialog(args: Map<String, String>) {
        val systemImpl : UstadMobileSystemImpl = di.direct.instance()
        systemImpl.go(DownloadDialogView.VIEW_NAME, args, requireContext())
    }

    override var title: String? = null
        set(value) {
            ustadFragmentTitle = value
            field = value
        }

    override var editOptionVisible: Boolean = false
        set(value) {
            activity?.invalidateOptionsMenu()
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val mTitle = arguments?.get(ARG_PARENT_ENTRY_TITLE)
        if(mTitle != null){
            ustadFragmentTitle = mTitle.toString()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = ContentEntryList2Presenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = ContentEntryListRecyclerAdapter(mPresenter,
                arguments?.toStringMap()?.determineListMode().toString(),
                arguments?.get(ARG_SELECT_FOLDER_VISIBLE)?.toString()?.toBoolean(),
                viewLifecycleOwner, di)

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_new_content),
            onClickSort = this,
            onFilterOptionSelected = mPresenter,
            sortOrderOption = mPresenter?.sortOptions?.get(0))

        setHasOptionsMenu(true)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_entrylist_options, menu)
        menu.findItem(R.id.edit).isVisible = editOptionVisible
        menu.findItem(R.id.hidden_items).isVisible = editOptionVisible
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                getString(R.string.content)
    }

    override fun showContentEntryAddOptions() {
        val entryAddOption = ContentEntryAddOptionsBottomSheetFragment(mPresenter)
        val args = mutableMapOf(ARG_SHOW_ADD_FOLDER to true.toString())
        entryAddOption.arguments = args.toBundle()
        entryAddOption.show(childFragmentManager, entryAddOption.tag)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                mPresenter?.handleClickEditFolder()
                return true
            }
            R.id.hidden_items -> {
                mPresenter?.handleClickShowHiddenItems()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.contentEntryDao



    companion object {

        @JvmField
        val CONTENT_ENTRY_TYPE_ICON_MAP = mapOf(
                ContentEntry.TYPE_EBOOK to R.drawable.ic_book_black_24dp,
                ContentEntry.TYPE_VIDEO to R.drawable.video_youtube,
                ContentEntry.TYPE_DOCUMENT to R.drawable.text_doc_24px,
                ContentEntry.TYPE_ARTICLE to R.drawable.article_24px,
                ContentEntry.TYPE_COLLECTION to R.drawable.collections_24px,
                ContentEntry.TYPE_INTERACTIVE_EXERCISE to R.drawable.ic_baseline_touch_app_24,
                ContentEntry.TYPE_AUDIO to R.drawable.ic_audiotrack_24px
        )

        @JvmField
        val CONTENT_ENTRY_TYPE_LABEL_MAP = mapOf(
                ContentEntry.TYPE_EBOOK to MessageID.ebook,
                ContentEntry.TYPE_VIDEO to MessageID.video,
                ContentEntry.TYPE_DOCUMENT to MessageID.document,
                ContentEntry.TYPE_ARTICLE to MessageID.article,
                ContentEntry.TYPE_COLLECTION to MessageID.collection,
                ContentEntry.TYPE_INTERACTIVE_EXERCISE to MessageID.interactive,
                ContentEntry.TYPE_AUDIO to MessageID.audio
        )

        val DIFF_CALLBACK: DiffUtil.ItemCallback<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> = object
            : DiffUtil.ItemCallback<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>() {
            override fun areItemsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                         newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                return oldItem.contentEntryUid == newItem.contentEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                            newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                return oldItem.title == newItem.title &&
                        oldItem.description == newItem.description &&
                        oldItem.contentTypeFlag == newItem.contentTypeFlag &&
                        oldItem.mostRecentContainer?.fileSize == newItem.mostRecentContainer?.fileSize &&
                        oldItem.ceInactive == newItem.ceInactive
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ContentEntryListScreen(
    uiState: ContentEntryListUiState = ContentEntryListUiState(),
    onClickContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit = {},
    onClickDownloadContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    )  {

        items(
            items = uiState.contentEntryList,
            key = { contentEntry -> contentEntry.contentEntryUid }
        ){ contentEntry ->

            ListItem(
                modifier = Modifier
                    .alpha((uiState.containerAlpha(contentEntry)).toFloat())
                    .clickable {
                        onClickContentEntry(contentEntry)
                    },

                text = { Text(contentEntry.title ?: "") },
                icon = {
                    LeadingContent(
                        uiState = uiState,
                        contentEntry = contentEntry
                    )
                },
                secondaryText = {
                    SecondaryContent(
                        contentEntry = contentEntry,
                        uiState = uiState
                    )
                },
                trailing = {
                    SecondaryAction(
                        onClick = onClickDownloadContentEntry,
                        contentEntry = contentEntry
                    )
                }
            )
        }
    }
}

@Composable
fun LeadingContent(
    uiState: ContentEntryListUiState,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
){

    val thumbnail: ImageVector = if (contentEntry.leaf)
        Icons.Outlined.Book
    else
        Icons.Default.Folder

    val badge = if (contentEntry.scoreProgress?.progressBadge() == ProgressConstants.BADGE_CHECK)
        R.drawable.ic_content_complete
    else
        R.drawable.ic_content_fail

    Column(
        modifier = Modifier.width(45.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ){
        Box(modifier = Modifier
            .size(35.dp)
            .align(Alignment.CenterHorizontally)
            .border(
                BorderStroke(1.dp, MaterialTheme.colors.onSurface), CircleShape
            ),
            contentAlignment = Alignment.Center
        ){
            Icon(
                thumbnail,
                contentDescription = "",
                modifier = Modifier
                    .padding(4.dp),
            )
        }

        BadgedBox(badge = {
            if (contentEntry.scoreProgress?.progressBadge() != ProgressConstants.BADGE_NONE){
                Image(
                    modifier = Modifier
                        .size(20.dp),
                    painter = painterResource(id = badge),
                    contentDescription = ""
                )
            }
        }) {
            if (uiState.progressVisible(contentEntry)){
                LinearProgressIndicator(
                    progress = ((contentEntry.scoreProgress?.progress ?: 0)/100.0)
                        .toFloat(),
                    modifier = Modifier
                        .height(4.dp)
                        .padding(end = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SecondaryContent(
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
    uiState: ContentEntryListUiState
){
    Column {
        Text((contentEntry.description ?: ""))

        Spacer(modifier = Modifier.height(5.dp))

        Row {

            if (uiState.mimetypeVisible(contentEntry)){
                Image(painter = painterResource(id =
                CONTENT_ENTRY_TYPE_ICON_MAP[contentEntry.contentTypeFlag]
                    ?: ContentEntry.TYPE_EBOOK),
                    contentDescription = "",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    messageIdResource(id = ContentEntryTypeLabelConstants
                        .TYPE_LABEL_MESSAGE_IDS[contentEntry.contentTypeFlag]
                        .messageId)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = ""
            )

            Text("${contentEntry.scoreProgress?.progress ?: 0}%")

            Text(uiState.scoreResultText(contentEntry))
        }
    }
}

@Composable
fun SecondaryAction(
    onClick: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
){
    IconButton(
        onClick = { onClick(contentEntry) },
    ) {

        CircularProgressIndicator(
            progress = ((contentEntry.scoreProgress?.progress ?: 0) / 100.0)
                .toFloat(),
            color = MaterialTheme.colors.secondary
        )

        Icon(
            Icons.Filled.FileDownload,
            contentDescription = ""
        )
    }

}

@Composable
@Preview
private fun ContentEntryListScreenPreview() {
    val uiStateVal = ContentEntryListUiState(
        contentEntryList = listOf(
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                contentEntryUid = 1
                leaf = false
                ceInactive = true
                scoreProgress = ContentEntryStatementScoreProgress().apply {
                    progress = 10
                    penalty = 20
                    success = RESULT_SUCCESS
                }
                contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                title = "Content Title 1"
                description = "Content Description 1"
            },
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                contentEntryUid = 2
                leaf = true
                ceInactive = false
                scoreProgress = ContentEntryStatementScoreProgress().apply {
                    progress = 60
                    penalty = 20
                }
                contentTypeFlag = ContentEntry.TYPE_DOCUMENT
                title = "Content Title 2"
                description = "Content Description 2"
            },
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                contentEntryUid = 3
                leaf = true
                ceInactive = false
                contentTypeFlag = ContentEntry.TYPE_DOCUMENT
                title = "Content Title 3"
                description = "Content Description 3"
            }
        ),
    )
    MdcTheme {
        ContentEntryListScreen(uiStateVal)
    }
}