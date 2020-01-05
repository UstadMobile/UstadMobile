package com.ustadmobile.core.controller

import com.github.aakira.napier.Napier
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_EDIT_BUTTONS_CONTROL_FLAG
import com.ustadmobile.core.view.ContentEntryListView.Companion.EDIT_BUTTONS_ADD_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.EDIT_BUTTONS_EDITOPTION
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class ContentEntryListPresenter(context: Any, arguments: Map<String, String?>,
                                private val viewContract: ContentEntryListView,
                                private val contentEntryDao: ContentEntryDao,
                                private val contentEntryDaoRepo: ContentEntryDao,
                                private val activeAccount: UmAccount?,
                                private val systemImpl: UstadMobileSystemImpl,
                                private val umRepo: UmAppDatabase)
    : UstadBaseController<ContentEntryListView>(context, arguments, viewContract) {

    private var filterByLang: Long = 0

    private var filterByCategory: Long = 0

    private var parentUid: Long = 0L

    private var noIframe: Boolean = false

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        when {
            arguments.containsKey(ARG_LIBRARIES_CONTENT) -> showContentByParent()
            arguments.containsKey(ARG_DOWNLOADED_CONTENT) -> showDownloadedContent()
            arguments.containsKey(ARG_RECYCLED_CONTENT) -> showRecycledEntries()
        }

        GlobalScope.launch {


            if (activeAccount != null) {
                val person = umRepo.personDao.findByUid(activeAccount.personUid)
                if (person?.admin == true) {

                    var contentEditFlags: Int = arguments[ARG_EDIT_BUTTONS_CONTROL_FLAG]?.toInt()
                            ?: 0
                    view.runOnUiThread(Runnable {
                        view.setEditButtonsVisibility(contentEditFlags)
                    })
                }
            }
        }

    }


    private fun onContentEntryChanged(entry: ContentEntry?) {
        if (entry == null) {
            return
        }
        val resultTitle = entry.title
        viewContract.runOnUiThread(Runnable {
            if (resultTitle != null)
                viewContract.setToolbarTitle(resultTitle)
        })

        val domains = systemImpl.getAppConfigString(
                AppConfig.KEY_NO_IFRAME, "", context)!!.split(",")

        noIframe = domains.contains(entry.publisher)

    }


    private fun showContentByParent() {
        parentUid = arguments.getValue(ARG_CONTENT_ENTRY_UID)?.toLong() ?: 0L
        val provider = contentEntryDaoRepo.getChildrenByParentUidWithCategoryFilter(parentUid, 0, 0, activeAccount?.personUid
                ?: 0)
        viewContract.setContentEntryProvider(provider)

        try {
            val entryLiveData: DoorLiveData<ContentEntry?> = contentEntryDaoRepo.findLiveContentEntry(parentUid)
            entryLiveData.observe(this, this::onContentEntryChanged)
        } catch (e: Exception) {
            viewContract.runOnUiThread(Runnable { viewContract.showError() })
        }


        val updateViewFn: (langList: MutableList<LangUidAndName>) -> Unit = { langList ->
            // if only English available, no need to show the spinner
            if (langList.size > 1) {
                val selectLang = LangUidAndName()
                selectLang.langName = "Language"
                selectLang.langUid = 0
                langList.add(0, selectLang)

                val allLang = LangUidAndName()
                allLang.langName = "All"
                allLang.langUid = 0
                langList.add(1, allLang)

                viewContract.runOnUiThread(Runnable {
                    viewContract.setLanguageOptions(langList)
                })

            }
        }

        GlobalScope.launch {
            val localResult = contentEntryDao.findUniqueLanguageWithParentUid(parentUid)
            updateViewFn(localResult.toMutableList())

            try {
                val remoteResult = contentEntryDaoRepo.findUniqueLanguageWithParentUid(parentUid).toMutableList()
                if (remoteResult != localResult) {
                    updateViewFn(remoteResult)
                }
            }catch(e: Exception) {
                Napier.e({"Exception loading language list"}, e)
            }

            contentEntryDaoRepo.findUniqueLanguagesInListAsync(parentUid)
        }

        GlobalScope.launch {
            try {
                val result = contentEntryDaoRepo.findListOfCategoriesAsync(parentUid)
                val schemaMap = HashMap<Long, List<DistinctCategorySchema>>()
                for (schema in result) {
                    var data: MutableList<DistinctCategorySchema>? =
                            schemaMap[schema.contentCategorySchemaUid] as MutableList<DistinctCategorySchema>?
                    if (data == null) {
                        data = ArrayList()
                        val schemaTitle = DistinctCategorySchema()
                        schemaTitle.categoryName = schema.schemaName
                        schemaTitle.contentCategoryUid = 0
                        schemaTitle.contentCategorySchemaUid = 0
                        data.add(0, schemaTitle)

                        val allSchema = DistinctCategorySchema()
                        allSchema.categoryName = "All"
                        allSchema.contentCategoryUid = 0
                        allSchema.contentCategorySchemaUid = 0
                        data.add(1, allSchema)

                    }
                    data.add(schema)
                    schemaMap[schema.contentCategorySchemaUid] = data
                }
                viewContract.setCategorySchemaSpinner(schemaMap)
            }catch(e: Exception) {
                Napier.e({"Exception loading list of categories"}, e)
            }
        }
    }

    private fun showDownloadedContent() {
        viewContract.setContentEntryProvider(contentEntryDao.downloadedRootItems())
    }

    private fun showRecycledEntries() {
        viewContract.setContentEntryProvider(contentEntryDaoRepo.recycledItems())
    }


    @JsName("handleContentEntryClicked")
    fun handleContentEntryClicked(entry: ContentEntry) {
        val args = hashMapOf<String, String?>()
        args.putAll(arguments)
        val entryUid = entry.contentEntryUid
        args[ARG_CONTENT_ENTRY_UID] = entryUid.toString()
        args[ARG_NO_IFRAMES] = noIframe.toString()
        args[ARG_EDIT_BUTTONS_CONTROL_FLAG] = (EDIT_BUTTONS_ADD_CONTENT or EDIT_BUTTONS_EDITOPTION).toString()
        val destView = if (entry.leaf) ContentEntryDetailView.VIEW_NAME else ContentEntryListView.VIEW_NAME
        systemImpl.go(destView, args, context)

    }

    @JsName("handleClickFilterByLanguage")
    fun handleClickFilterByLanguage(langUid: Long) {
        this.filterByLang = langUid
        viewContract.setContentEntryProvider(contentEntryDaoRepo.getChildrenByParentUidWithCategoryFilter(parentUid, filterByLang, filterByCategory, activeAccount?.personUid
                ?: 0))
    }

    @JsName("handleClickFilterByCategory")
    fun handleClickFilterByCategory(contentCategoryUid: Long) {
        this.filterByCategory = contentCategoryUid
        viewContract.setContentEntryProvider(contentEntryDaoRepo.getChildrenByParentUidWithCategoryFilter(parentUid, filterByLang, filterByCategory, activeAccount?.personUid
                ?: 0))
    }

    @JsName("handleUpNavigation")
    fun handleUpNavigation() {
        systemImpl.go(HomeView.VIEW_NAME, mapOf(), view.viewContext,
                UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
    }

    @JsName("handleDownloadStatusButtonClicked")
    fun handleDownloadStatusButtonClicked(entry: ContentEntry) {
        systemImpl.go("DownloadDialog",
                mapOf(ARG_CONTENT_ENTRY_UID to entry.contentEntryUid.toString()), context)
    }


    fun handleClickAddContent(contentType: Int) {
        val args = HashMap<String, String?>()
        args.putAll(arguments)
        args[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = parentUid.toString()
        args[ARG_CONTENT_ENTRY_UID] = 0.toString()
        args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()
        args[ContentEntryEditView.CONTENT_TYPE] = contentType.toString()

        when (contentType) {
            ContentEntryListView.CONTENT_CREATE_FOLDER -> {
                args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = false.toString()
                systemImpl.go(ContentEntryEditView.VIEW_NAME, args, this.context)
            }

            ContentEntryListView.CONTENT_IMPORT_FILE -> {
                systemImpl.go(ContentEntryEditView.VIEW_NAME, args, this.context)
            }

            ContentEntryListView.CONTENT_CREATE_CONTENT -> {
                systemImpl.go(ContentEntryEditView.VIEW_NAME, args, this.context)
            }
            ContentEntryListView.CONTENT_IMPORT_LINK -> {
                systemImpl.go(ContentEntryImportLinkView.VIEW_NAME, args, this.context)
            }
        }
    }

    fun handleClickEditButton() {
        val args = HashMap<String, String?>()
        args.putAll(arguments)
        args[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = parentUid.toString()
        args[ARG_CONTENT_ENTRY_UID] = parentUid.toString()
        args[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_CREATE_FOLDER.toString()
        args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = false.toString()
        systemImpl.go(ContentEntryEditView.VIEW_NAME, args, context)
    }

    companion object {

        const val ARG_NO_IFRAMES = "noiframe"

        const val ARG_DOWNLOADED_CONTENT = "downloaded"

        const val ARG_RECYCLED_CONTENT = "recycled"

        const val ARG_LIBRARIES_CONTENT = "libraries"
    }
}
