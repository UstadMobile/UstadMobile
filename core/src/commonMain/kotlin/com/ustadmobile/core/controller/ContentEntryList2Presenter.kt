package com.ustadmobile.core.controller

import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.SupportedContent.EPUB_EXTENSIONS
import com.ustadmobile.core.contentjob.SupportedContent.EPUB_MIME_TYPES
import com.ustadmobile.core.contentjob.SupportedContent.H5P_EXTENSIONS
import com.ustadmobile.core.contentjob.SupportedContent.H5P_MIME_TYPES
import com.ustadmobile.core.contentjob.SupportedContent.PDF_EXTENSIONS
import com.ustadmobile.core.contentjob.SupportedContent.PDF_MIME_TYPES
import com.ustadmobile.core.contentjob.SupportedContent.XAPI_MIME_TYPES
import com.ustadmobile.core.contentjob.SupportedContent.ZIP_EXTENSIONS
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.UmPlatformUtil
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_OPTION
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_PARENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_SHOW_ONLY_FOLDER_FILTER
import com.ustadmobile.core.view.SelectFileView.Companion.SELECTION_MODE_GALLERY
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on

class ContentEntryList2Presenter(context: Any, arguments: Map<String, String>, view: ContentEntryList2View,
                                 di: DI, lifecycleOwner: LifecycleOwner,
                                 val contentEntryListItemListener: DefaultContentEntryListItemListener
                                 = DefaultContentEntryListItemListener(view = view, context = context,
                                         di = di, clazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L))
    : UstadListPresenter<ContentEntryList2View, ContentEntry>(context, arguments, view, di, lifecycleOwner),
        ContentEntryListItemListener by contentEntryListItemListener, ContentEntryAddOptionsListener {

    private val navController: UstadNavController by instance()

    private val pluginManager: ContentPluginManager? by on(accountManager.activeAccount).instanceOrNull()

    private var contentFilter = ARG_DISPLAY_CONTENT_BY_PARENT

    private var onlyFolderFilter = false

    private var loggedPersonUid: Long = 0L

    private val parentEntryUidStack = mutableListOf<Long>()

    private var movingSelectedItems: List<Long>? = null

    private val parentEntryUid: Long
        get() = parentEntryUidStack.lastOrNull() ?: 0L


    private var selectedChipOption: Int = 0

    private val editVisible = CompletableDeferred<Boolean>()

    private var showHiddenEntries = false

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
        loggedPersonUid = accountManager.activeAccount.personUid
        showHiddenEntries = false
        view.listFilterOptionChips = if(arguments[ContentEntryList2View.ARG_USE_CHIPS] == true.toString()) {
            PICKER_CHIP_OPTIONS.map {
                ListFilterIdOption(systemImpl.getString(it.first, context), it.second)
            }
        }else {
            listOf()
        }

        getAndSetList()
        presenterScope.launch(doorMainDispatcher()) {
            if (contentFilter == ARG_DISPLAY_CONTENT_BY_PARENT) {
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
        view.list = when {
            selectedChipOption == CHIP_ID_MY_CONTENT -> {
                repo.contentEntryDao.getContentByOwner(loggedPersonUid)
            }
            selectedChipOption == CHIP_ID_FROM_MY_COURSES -> {
                repo.contentEntryDao.getContentFromMyCourses(loggedPersonUid)
            }
            selectedChipOption == CHIP_ID_LIBRARY -> {
                repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByName(
                    parentEntryUid, 0, 0, loggedPersonUid, false, onlyFolderFilter,
                    selectedSortOption?.flag ?: ContentEntryDaoCommon.SORT_TITLE_ASC)
            }
            contentFilter == ARG_DISPLAY_CONTENT_BY_PARENT -> {
                repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByName(
                        parentEntryUid, 0, 0, loggedPersonUid, showHiddenEntries, onlyFolderFilter,
                        selectedSortOption?.flag ?: ContentEntryDaoCommon.SORT_TITLE_ASC)
            }
            else -> null
        }

        presenterScope.launch(doorMainDispatcher()) {
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
            }else -> {
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
        }
    }

    override fun handleClickSelectionOption(selectedItem: List<ContentEntry>, option: SelectionOption) {
        val selectedContentEntryUids = selectedItem.map { it.contentEntryUid }
        val selectedContentEntryParentChildUids = selectedItem.mapNotNull {
            (it as? ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)?.contentEntryParentChildJoin?.cepcjUid
        }

        presenterScope.launch(doorMainDispatcher()) {
            when (option) {
                SelectionOption.MOVE -> {
                    handleClickMove(selectedContentEntryParentChildUids)
                }
                SelectionOption.HIDE -> {
                    when(contentFilter){
                        ARG_DISPLAY_CONTENT_BY_PARENT ->{
                            repo.contentEntryDao.toggleVisibilityContentEntryItems(true,
                                selectedContentEntryUids, systemTimeInMillis())
                        }
                    }
                }
                SelectionOption.UNHIDE -> {
                    repo.contentEntryDao.toggleVisibilityContentEntryItems(false,
                        selectedContentEntryUids, systemTimeInMillis())
                }
                else -> {
                    //Do nothing
                }
            }
        }
    }

    override fun onListFilterOptionSelected(filterOptionId: ListFilterIdOption) {
        selectedChipOption = filterOptionId.optionId
        parentEntryUidStack.clear()
        if(filterOptionId.optionId == CHIP_ID_LIBRARY) {
            presenterScope.launch {
                view.addMode = if(onCheckUpdatePermission()) {
                    ListViewAddMode.FIRST_ITEM
                }else {
                    ListViewAddMode.NONE
                }
            }

            parentEntryUidStack += MASTER_SERVER_ROOT_ENTRY_UID
        }else {
            //If any chip other than library was selected, new content created should not have
            // any parentEntryUid
            parentEntryUidStack += 0L
            view.addMode = ListViewAddMode.FIRST_ITEM
        }

        getAndSetList()
    }

    /**
     * Show ContentEntryList in picker mode so the user can select a folder to move entries to.
     *
     * @param childrenToMove list of content entry parent child join uids that will be
     * moved
     */
    private fun handleClickMove(childrenToMove: List<Long>){
        val args = mutableMapOf(ARG_PARENT_ENTRY_UID to MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                ARG_DISPLAY_CONTENT_BY_OPTION to ARG_DISPLAY_CONTENT_BY_PARENT,
                ARG_SHOW_ONLY_FOLDER_FILTER to true.toString(),
                KEY_SELECTED_ITEMS to childrenToMove.joinToString(","))

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
        if (parentChildJoinUids.isNotEmpty()) {
            presenterScope.launch(doorMainDispatcher()) {

                repo.contentEntryParentChildJoinDao.moveListOfEntriesToNewParent(
                    destContentEntryUid, parentChildJoinUids, systemTimeInMillis())

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
                sortOrder = selectedSortOption?.flag ?: ContentEntryDaoCommon.SORT_TITLE_ASC)
    }

    fun handleOnBackPressed(): Boolean {
        if (mListMode == ListViewMode.PICKER && parentEntryUidStack.count() > 1) {
            parentEntryUidStack.removeAt(parentEntryUidStack.count() - 1)
            showContentEntryListByParentUid()
            return true
        }
        return false
    }

    fun handleEntrySelectedFromPicker(entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) {
        if(arguments.containsKey(ContentEntryEdit2View.BLOCK_REQUIRED)){
            val args = mutableMapOf<String, String>()
            args[ARG_LEAF] = true.toString()
            args[ARG_PARENT_ENTRY_UID] = parentEntryUid.toString()
            args[ARG_ENTITY_UID] = entry.contentEntryUid.toString()
            args.putFromOtherMapIfPresent(arguments, ContentEntryEdit2View.BLOCK_REQUIRED)
            args.putFromOtherMapIfPresent(arguments, ARG_CLAZZUID)

            navigateForResult(
                NavigateForResultOptions(
                    this, null,
                    ContentEntryEdit2View.VIEW_NAME,
                    ContentEntryWithBlockAndLanguage::class,
                    ContentEntryWithBlockAndLanguage.serializer(),
                    arguments = args
                )
            )
        }else{
            finishWithResult(
                safeStringify(di, ListSerializer(ContentEntry.serializer()), listOf(entry))
            )
        }
    }

    override fun handleClickCreateNewFab() {
        view.showContentEntryAddOptions()
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }


    fun handleClickEditFolder() {
        val args = mutableMapOf(ARG_ENTITY_UID to parentEntryUid.toString())

        navigateForResult(
            NavigateForResultOptions(this,
                null, ContentEntryEdit2View.VIEW_NAME,
                ContentEntry::class,
                ContentEntry.serializer(),
                arguments = args)
        )
    }

    fun handleClickShowHiddenItems() {
        showHiddenEntries = true
        getAndSetList()
    }

    override fun onClickNewFolder() {
        val args = mutableMapOf(
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to false.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navigateForResult(NavigateForResultOptions(
            this, null,
            ContentEntryEdit2View.VIEW_NAME,
            ContentEntry::class,
            ContentEntry.serializer(),
            arguments = args)
        )
    }


    fun handleOnClickAddSupportedFile(){
        val supportedMimeTypes = mutableListOf(
            EPUB_MIME_TYPES.joinToString(";"),
            EPUB_EXTENSIONS.joinToString(";"),
            XAPI_MIME_TYPES.joinToString(";"),
            ZIP_EXTENSIONS.joinToString(";"),
            H5P_MIME_TYPES.joinToString(";"),
            H5P_EXTENSIONS.joinToString(";"),
            PDF_EXTENSIONS.joinToString(";"),
            PDF_MIME_TYPES.joinToString(";"),
            SELECTION_MODE_GALLERY
        ).joinToString (";")
        val args = mutableMapOf(
            SelectFileView.ARG_MIMETYPE_SELECTED to supportedMimeTypes,
            ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
            ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navigateForResult(NavigateForResultOptions(
            this, null,
            SelectFileView.VIEW_NAME,
            ContentEntry::class,
            ContentEntry.serializer(),
            arguments = args)
        )
    }

    override fun onClickImportFile() {
        val args = mutableMapOf(
                SelectFileView.ARG_MIMETYPE_SELECTED to
                        (pluginManager?.supportedMimeTypeList?.joinToString(";") ?: "*/*"),
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)
        args.putFromOtherMapIfPresent(arguments, ContentEntryEdit2View.BLOCK_REQUIRED)
        args.putFromOtherMapIfPresent(arguments, ARG_CLAZZUID)

        navigateForResult(NavigateForResultOptions(
            this, null,
            SelectExtractFileView.VIEW_NAME,
            ContentEntry::class,
            ContentEntry.serializer(),
            arguments = args)
        )
    }

    override fun onClickImportLink() {
        val args = mutableMapOf(
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)
        args.putFromOtherMapIfPresent(arguments, ContentEntryEdit2View.BLOCK_REQUIRED)
        args.putFromOtherMapIfPresent(arguments, ARG_CLAZZUID)

        navigateForResult(NavigateForResultOptions(
            this, null,
            ContentEntryImportLinkView.VIEW_NAME,
            ContentEntry::class,
            ContentEntry.serializer(),
            arguments = args)
        )
    }

    override fun onClickImportGallery() {
        val args = mutableMapOf(
                SelectFileView.ARG_MIMETYPE_SELECTED to SELECTION_MODE_GALLERY,
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)
        args.putFromOtherMapIfPresent(arguments, ContentEntryEdit2View.BLOCK_REQUIRED)
        args.putFromOtherMapIfPresent(arguments, ARG_CLAZZUID)

        navigateForResult(NavigateForResultOptions(
            this, null,
            SelectExtractFileView.VIEW_NAME,
            ContentEntry::class,
            ContentEntry.serializer(),
            arguments = args)
        )
    }

    override fun onClickAddFolder() {
        val args = mutableMapOf(
                ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, KEY_SELECTED_ITEMS)

        navigateForResult(NavigateForResultOptions(
            this, null,
            SelectFolderView.VIEW_NAME,
            ContentEntry::class,
            ContentEntry.serializer(),
            arguments = args)
        )
    }

    fun handleMoveWithSelectedEntry(
        entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) {
        presenterScope.launch(doorMainDispatcher()) {
            if (arguments.containsKey(KEY_SELECTED_ITEMS)) {
                val selectedItems = arguments[KEY_SELECTED_ITEMS]?.split(",")
                    ?.map { it.trim().toLong() } ?: listOf()
                repo.contentEntryParentChildJoinDao.moveListOfEntriesToNewParent(
                    entry.contentEntryUid, selectedItems, systemTimeInMillis())
            }
        }
    }


    companion object {


        /**
         * Key used when saving selected items to the savedStateHandle
         */
        const val KEY_SELECTED_ITEMS = "selected_items"

        const val SAVEDSTATE_KEY_FOLDER = "Folder_ContentEntry"

        const val CHIP_ID_MY_CONTENT = 1

        const val CHIP_ID_FROM_MY_COURSES = 2

        const val CHIP_ID_LIBRARY = 3

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.title, ContentEntryDaoCommon.SORT_TITLE_ASC, true),
                SortOrderOption(MessageID.title, ContentEntryDaoCommon.SORT_TITLE_DESC, false)
        )

        val PICKER_CHIP_OPTIONS = listOf(
            MessageID.my_content to CHIP_ID_MY_CONTENT,
            MessageID.from_my_classes to CHIP_ID_FROM_MY_COURSES,
            MessageID.library to CHIP_ID_LIBRARY,
        )

    }


}