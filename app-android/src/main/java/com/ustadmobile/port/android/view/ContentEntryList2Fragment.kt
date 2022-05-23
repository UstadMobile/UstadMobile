package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.ext.determineListMode
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SELECT_FOLDER_VISIBLE
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_USE_CHIPS
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_TITLE
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.view.ContentEntryAddOptionsBottomSheetFragment.Companion.ARG_SHOW_ADD_FOLDER
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