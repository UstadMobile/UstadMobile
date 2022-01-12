package com.ustadmobile.core.controller

import SelectFolderView
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.UmPlatform
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_CLAZZ
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_OPTION
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_PARENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SHOW_ONLY_FOLDER_FILTER
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class ContentEntryList2Presenter(context: Any, arguments: Map<String, String>, view: ContentEntryList2View,
                                 di: DI, lifecycleOwner: DoorLifecycleOwner,
                                 val contentEntryListItemListener: DefaultContentEntryListItemListener
                                 = DefaultContentEntryListItemListener(view = view, context = context,
                                         di = di, clazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L))
    : UstadListPresenter<ContentEntryList2View, ContentEntry>(context, arguments, view, di, lifecycleOwner),
        ContentEntryListItemListener by contentEntryListItemListener, ContentEntryAddOptionsListener {

    private val navController: UstadNavController by instance()

    private var contentFilter = ARG_DISPLAY_CONTENT_BY_PARENT

    private var onlyFolderFilter = false

    private var loggedPersonUid: Long = 0L

    private val parentEntryUidStack = mutableListOf<Long>()

    private var movingSelectedItems: List<Long>? = null

    private val parentEntryUid: Long
        get() = parentEntryUidStack.lastOrNull() ?: 0L

    private val editVisible = CompletableDeferred<Boolean>()

    private var showHiddenEntries = false

    private var selectedClazzUid: Long = 0L

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        contentEntryListItemListener.mListMode = mListMode
        contentEntryListItemListener.presenter = this
        selectedSortOption = SORT_OPTIONS[0]
        contentFilter = arguments[ARG_DISPLAY_CONTENT_BY_OPTION] ?: ARG_DISPLAY_CONTENT_BY_PARENT
        onlyFolderFilter = arguments[ARG_SHOW_ONLY_FOLDER_FILTER]?.toBoolean() ?: false
        parentEntryUidStack += arguments[ARG_PARENT_ENTRY_UID]?.toLongOrNull() ?: MASTER_SERVER_ROOT_ENTRY_UID
        selectedClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
        loggedPersonUid = accountManager.activeAccount.personUid
        showHiddenEntries = false
        getAndSetList()
        GlobalScope.launch(doorMainDispatcher()) {
            if (contentFilter == ARG_DISPLAY_CONTENT_BY_PARENT || contentFilter == ARG_DISPLAY_CONTENT_BY_CLAZZ) {
                view.editOptionVisible = onCheckUpdatePermission()
                editVisible.complete(view.editOptionVisible)
            }
        }

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(accountManager.activeAccount.personUid,
                Role.PERMISSION_CONTENT_INSERT)
    }

    suspend fun onCheckUpdatePermission(): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(accountManager.activeAccount.personUid,
                Role.PERMISSION_CONTENT_UPDATE)
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        getAndSetList()
    }


    private fun getAndSetList() {
        view.list = when (contentFilter) {
            ARG_DISPLAY_CONTENT_BY_PARENT -> {
                repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByName(
                        parentEntryUid, 0, 0, loggedPersonUid, showHiddenEntries, onlyFolderFilter,
                        selectedSortOption?.flag ?: ContentEntryDao.SORT_TITLE_ASC)
            }
            ARG_DISPLAY_CONTENT_BY_CLAZZ -> {
                repo.contentEntryDao.getClazzContent(selectedClazzUid, loggedPersonUid, showHiddenEntries,
                        selectedSortOption?.flag ?: ContentEntryDao.SORT_TITLE_ASC)
            }
            else -> null
        }

        GlobalScope.launch(doorMainDispatcher()) {
            db.takeIf { parentEntryUid != 0L }?.contentEntryDao?.findTitleByUidAsync(parentEntryUid)?.let { titleStr ->
                view.takeIf { titleStr.isNotBlank() }?.title = titleStr
            }
        }
    }

    override suspend fun onCheckListSelectionOptions(account: UmAccount?): List<SelectionOption> {
        val isVisible = editVisible.await()
        return when(contentFilter) {
            ARG_DISPLAY_CONTENT_BY_PARENT -> {
                if (isVisible) listOf(SelectionOption.MOVE, SelectionOption.HIDE) else listOf()
            }
            ARG_DISPLAY_CONTENT_BY_CLAZZ -> {
                if(isVisible) listOf(SelectionOption.HIDE) else listOf()

            }
            else -> {
                listOf()
            }
        }
    }

    override fun handleSelectionOptionChanged(t: List<ContentEntry>) {
        if (!view.editOptionVisible) {
            return
        }

       when(contentFilter){

            ARG_DISPLAY_CONTENT_BY_PARENT ->{
                view.selectionOptions = if (t.all { it.ceInactive })
                    listOf(SelectionOption.MOVE, SelectionOption.UNHIDE)
                else
                    listOf(SelectionOption.MOVE, SelectionOption.HIDE)
            }
           ARG_DISPLAY_CONTENT_BY_CLAZZ ->{
                view.selectionOptions = listOf(SelectionOption.HIDE)
            }

        }
    }

    override fun handleClickSelectionOption(selectedItem: List<ContentEntry>, option: SelectionOption) {
        val selectedEntryIds = selectedItem.mapNotNull {
            (it as? ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)?.contentEntryParentChildJoin?.cepcjUid
        }.joinToString(",")

        val selectedUidList = selectedItem.map { it.contentEntryUid }

        GlobalScope.launch(doorMainDispatcher()) {
            when (option) {
                SelectionOption.MOVE -> {
                    UmPlatform.console(selectedEntryIds)
                    handleClickMove(selectedEntryIds)
                }
                SelectionOption.HIDE -> {
                    when(contentFilter){
                        ARG_DISPLAY_CONTENT_BY_PARENT ->{
                            repo.contentEntryDao.toggleVisibilityContentEntryItems(true, selectedUidList)
                        }
                        ARG_DISPLAY_CONTENT_BY_CLAZZ ->{
                            repo.clazzContentJoinDao.toggleVisibilityClazzContent(false, selectedUidList)
                        }
                    }
                }
                SelectionOption.UNHIDE -> {
                    repo.contentEntryDao.toggleVisibilityContentEntryItems(false, selectedItem.map { it.contentEntryUid })
                }
            }
        }
    }

    /**
     * Show ContentEntryList in picker mode so the user can select a folder to move entries to.
     *
     * @param childrenToMove list of child entries selected to move
     */
    private fun handleClickMove(childrenToMove: String){
        val args = mutableMapOf(ARG_PARENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                ARG_DISPLAY_CONTENT_BY_OPTION to ARG_DISPLAY_CONTENT_BY_PARENT,
                ARG_SHOW_ONLY_FOLDER_FILTER to true.toString(),
                KEY_SELECTED_ITEMS to childrenToMove)

        navigateForResult(
                NavigateForResultOptions(this,
                        null, ContentEntryList2View.FOLDER_VIEW_NAME,
                        ContentEntry::class,
                        ContentEntry.serializer(),
                        SAVEDSTATE_KEY_FOLDER,
                        overwriteDestination = true,
                        arguments = args))
    }


    fun handleMoveContentEntries(parentChildJoinUids: List<Long>, destContentEntryUid: Long) {
        if (!parentChildJoinUids.isNullOrEmpty()) {
            GlobalScope.launch(doorMainDispatcher()) {

                repo.contentEntryParentChildJoinDao.moveListOfEntriesToNewParent(destContentEntryUid, parentChildJoinUids)

                view.showSnackBar(systemImpl.getString(MessageID.moved_x_entries, context).replace("%1\$s",
                        parentChildJoinUids.size.toString()), actionMessageId = MessageID.open_folder,
                        action = {
                            systemImpl.go(ContentEntryList2View.VIEW_NAME,
                                    mapOf(ARG_PARENT_ENTRY_UID to destContentEntryUid.toString(),
                                            ARG_DISPLAY_CONTENT_BY_OPTION to ARG_DISPLAY_CONTENT_BY_PARENT), context)
                        })
            }
        }
    }

    /**
     * Handles when the user clicks a "folder" in picker mode
     */
    fun openContentEntryBranchPicker(entry: ContentEntry) {
        this.parentEntryUidStack += entry.contentEntryUid
        showContentEntryListByParentUid()
    }

    private fun showContentEntryListByParentUid() {
        view.list = repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByName(
                parentEntryUid, 0, 0, loggedPersonUid, showHidden = false, onlyFolder = false,
                sortOrder = selectedSortOption?.flag ?: ContentEntryDao.SORT_TITLE_ASC)
    }

    fun handleOnBackPressed(): Boolean {
        if (mListMode == ListViewMode.PICKER && parentEntryUidStack.count() > 1) {
            parentEntryUidStack.removeAt(parentEntryUidStack.count() - 1)
            showContentEntryListByParentUid()
            return true
        }
        return false
    }

    override fun handleClickCreateNewFab() {
        when (contentFilter) {
            ARG_DISPLAY_CONTENT_BY_CLAZZ -> {

                val args = mutableMapOf(
                        ARG_DISPLAY_CONTENT_BY_OPTION to ARG_DISPLAY_CONTENT_BY_PARENT,
                        ARG_PARENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString())
                navigateForResult(
                        NavigateForResultOptions(this,
                                null, ContentEntryList2View.VIEW_NAME,
                                ContentEntry::class,
                                ContentEntry.serializer(),
                                SAVEDSTATE_KEY_ENTRY,
                                arguments = args)
                )
            }
            else -> {
                view.showContentEntryAddOptions()
            }
        }

    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }


    fun handleClickEditFolder() {
        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to parentEntryUid.toString()), context)
    }

    fun handleClickShowHiddenItems() {
        showHiddenEntries = true
        getAndSetList()
    }

    override fun onClickNewFolder() {
        val args = mutableMapOf(
                SelectFileView.ARG_SELECTION_MODE to SelectFileView.SELECTION_MODE_FILE,
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to false.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navController.navigate(ContentEntryEdit2View.VIEW_NAME, args)
    }

    override fun onClickImportFile() {
        val args = mutableMapOf(
                SelectFileView.ARG_SELECTION_MODE to SelectFileView.SELECTION_MODE_FILE,
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navController.navigate(SelectFileView.VIEW_NAME, args)
    }

    override fun onClickImportLink() {
        val args = mutableMapOf(
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navController.navigate(ContentEntryImportLinkView.VIEW_NAME, args)
    }

    override fun onClickImportGallery() {
        val args = mutableMapOf(
                SelectFileView.ARG_SELECTION_MODE to SelectFileView.SELECTION_MODE_GALLERY,
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navController.navigate(SelectFileView.VIEW_NAME, args)
    }

    override fun onClickAddFolder() {
        val args = mutableMapOf(
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navController.navigate(SelectFolderView.VIEW_NAME, args)
    }

    fun handleMoveWithSelectedEntry(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {

        GlobalScope.launch(doorMainDispatcher()) {
            if (arguments.containsKey(KEY_SELECTED_ITEMS)) {
                val selectedItems = arguments[KEY_SELECTED_ITEMS]?.split(",")?.map { it.trim().toLong() }
                        ?: listOf()
                repo.contentEntryParentChildJoinDao.moveListOfEntriesToNewParent(entry.contentEntryUid, selectedItems)
            }
        }
    }


    companion object {


        /**
         * Key used when saving selected items to the savedStateHandle
         */
        const val KEY_SELECTED_ITEMS = "selected_items"

        const val SAVEDSTATE_KEY_ENTRY = "Clazz_ContentEntry"

        const val SAVEDSTATE_KEY_FOLDER = "Folder_ContentEntry"

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.title, ContentEntryDao.SORT_TITLE_ASC, true),
                SortOrderOption(MessageID.title, ContentEntryDao.SORT_TITLE_DESC, false)
        )

    }


}