package com.ustadmobile.port.android.view

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.util.ext.determineListMode
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SELECT_FOLDER_VISIBLE
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_TITLE
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ContentEntryList2Fragment : UstadListViewFragment<ContentEntry, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(),
        ContentEntryList2View, View.OnClickListener, FragmentBackHandler{

    private var mPresenter: ContentEntryList2Presenter? = null

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter


    override fun onHostBackPressed() = mPresenter?.handleOnBackPressed() ?: false

    private var localAvailabilityCallback: ContentEntryLocalAvailabilityPagedListCallback? = null

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
        mPresenter = ContentEntryList2Presenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = ContentEntryListRecyclerAdapter(mPresenter,
                arguments.toStringMap().determineListMode().toString(),
                arguments?.get(ARG_SELECT_FOLDER_VISIBLE).toString().toBoolean(),
                viewLifecycleOwner, di)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_new_content))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val accountManager: UstadAccountManager by di.instance()
        val localAvailabilityManager: LocalAvailabilityManager by di.on(accountManager.activeAccount).instance()

        val navController = findNavController()

        localAvailabilityCallback = ContentEntryLocalAvailabilityPagedListCallback(localAvailabilityManager,
                null) {availabilityMap ->
            GlobalScope.launch(Dispatchers.Main) {
                (mDataRecyclerViewAdapter as? ContentEntryListRecyclerAdapter)?.onLocalAvailabilityUpdated(availabilityMap)
            }
        }

        setHasOptionsMenu(true)

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
            ContentEntry::class.java) {
            val selectedParentChildJoinUids = navController.currentBackStackEntry?.savedStateHandle
                    ?.get<String>(KEY_SELECTED_ITEMS)?.split(",")?.map { it.trim().toLong() } ?: return@observeResult
            mPresenter?.handleMoveContentEntries(selectedParentChildJoinUids, it.first().contentEntryUid )
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_entrylist_options, menu)
        menu.findItem(R.id.edit).isVisible = editOptionVisible
        menu.findItem(R.id.hidden_items).isVisible = editOptionVisible
    }


    private var mCurrentPagedList: PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>? = null

    override fun onChanged(t: PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?) {
        super.onChanged(t)

        val localAvailabilityCallbackVal = localAvailabilityCallback
        if(localAvailabilityCallbackVal != null){
            mCurrentPagedList?.removeWeakCallback(localAvailabilityCallbackVal)
        }


        if(localAvailabilityCallbackVal != null && t != null) {
            localAvailabilityCallbackVal.pagedList = t
            t.addWeakCallback(listOf(), localAvailabilityCallbackVal)
        }
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                getString(R.string.content)
    }

    override fun showContentEntryAddOptions(parentEntryUid: Long) {
        val entryAddOption = ContentEntryAddOptionsBottomSheetFragment()
        entryAddOption.arguments = UMAndroidUtil.mapToBundle(mapOf(UstadView.ARG_PARENT_ENTRY_UID
                to parentEntryUid.toString()))
        entryAddOption.show(childFragmentManager, entryAddOption.tag)

    }

    /**
     * OnClick function that will handle
     * when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if (view?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickCreateNewFab()
    }

    override fun showMoveEntriesFolderPicker(selectedContentEntryParentChildJoinUids: String) {
        findNavController().currentBackStackEntry?.savedStateHandle?.set(
                KEY_SELECTED_ITEMS, selectedContentEntryParentChildJoinUids)
        navigateToPickEntityFromList(ContentEntry::class.java,
                R.id.content_entry_list_select_folder,
                bundleOf(UstadView.ARG_PARENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                        ContentEntryList2View.ARG_CONTENT_FILTER to ContentEntryList2View.ARG_LIBRARIES_CONTENT,
                        ContentEntryList2View.ARG_FOLDER_FILTER to true.toString()))
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
        localAvailabilityCallback?.onDestroy()
        localAvailabilityCallback = null
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

        /**
         * Key used when saving selected items to the savedStateHandle
         */
        const val KEY_SELECTED_ITEMS = "selected_items"


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
                        oldItem.thumbnailUrl == newItem.thumbnailUrl &&
                        oldItem.ceInactive == newItem.ceInactive
            }
        }
    }
}