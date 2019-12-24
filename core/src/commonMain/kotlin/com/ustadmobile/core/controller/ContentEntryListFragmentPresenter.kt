package com.ustadmobile.core.controller

import com.github.aakira.napier.Napier
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryListFragmentView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class ContentEntryListFragmentPresenter(context: Any, arguments: Map<String, String?>,
                                        private val fragmentViewContract: ContentEntryListFragmentView,
                                        private val contentEntryDao: ContentEntryDao,
                                        private val contentEntryDaoRepo: ContentEntryDao,
                                        private val activeAccount: UmAccount?)
    : UstadBaseController<ContentEntryListFragmentView>(context, arguments, fragmentViewContract) {

    private var filterByLang: Long = 0

    private var filterByCategory: Long = 0

    private var parentUid: Long? = null

    private var noIframe: Boolean = false

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        when {
            arguments.containsKey(ARG_LIBRARIES_CONTENT) -> showContentByParent()
            arguments.containsKey(ARG_DOWNLOADED_CONTENT) -> showDownloadedContent()
            arguments.containsKey(ARG_RECYCLED_CONTENT) -> showRecycledEntries()
        }

    }


    private fun onContentEntryChanged(entry: ContentEntry?) {
        if (entry == null) {
            fragmentViewContract.runOnUiThread(Runnable { fragmentViewContract.showError() })
            return
        }
        val resultTitle = entry.title

        val domains = UstadMobileSystemImpl.instance.getAppConfigString(
                AppConfig.KEY_NO_IFRAME, "", context)!!.split(",")

        noIframe = domains.contains(entry.publisher)
        if (resultTitle != null)
            fragmentViewContract.setToolbarTitle(resultTitle)
    }


    private fun showContentByParent() {
        parentUid = arguments.getValue(ARG_CONTENT_ENTRY_UID)!!.toLong()
        val provider = contentEntryDaoRepo.getChildrenByParentUidWithCategoryFilter(parentUid!!, 0, 0, activeAccount?.personUid ?: 0)
        fragmentViewContract.setContentEntryProvider(provider)

        try {
            val entryLiveData: DoorLiveData<ContentEntry?> = contentEntryDaoRepo.findLiveContentEntry(parentUid!!)
            entryLiveData.observe(this, this::onContentEntryChanged)
        } catch (e: Exception) {
            fragmentViewContract.runOnUiThread(Runnable { fragmentViewContract.showError() })
        }

        GlobalScope.launch {
            val result = contentEntryDaoRepo.findUniqueLanguagesInListAsync(parentUid!!).toMutableList()
            if (result.size > 1) {
                val selectLang = Language()
                selectLang.name = "Language"
                selectLang.langUid = 0
                result.add(0, selectLang)

                val allLang = Language()
                allLang.name = "All"
                allLang.langUid = 0
                result.add(1, allLang)

                fragmentViewContract.setLanguageOptions(result)
            }
        }

        GlobalScope.launch {
            val result = contentEntryDaoRepo.findListOfCategoriesAsync(parentUid!!)
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
            fragmentViewContract.setCategorySchemaSpinner(schemaMap)
        }
    }

    private fun showDownloadedContent() {
        fragmentViewContract.setContentEntryProvider(contentEntryDao.downloadedRootItems())
    }

    private fun showRecycledEntries() {
        fragmentViewContract.setContentEntryProvider(contentEntryDaoRepo.recycledItems())
    }


    @JsName("handleContentEntryClicked")
    fun handleContentEntryClicked(entry: ContentEntry) {
        val impl = UstadMobileSystemImpl.instance
        val args = hashMapOf<String, String?>()
        args.putAll(arguments)
        val entryUid = entry.contentEntryUid
        args[ARG_CONTENT_ENTRY_UID] = entryUid.toString()
        args[ARG_NO_IFRAMES] = noIframe.toString()
        val destView = if (entry.leaf) ContentEntryDetailView.VIEW_NAME else ContentEntryListView.VIEW_NAME
        impl.go(destView, args, view.viewContext)

    }

    @JsName("handleClickFilterByLanguage")
    fun handleClickFilterByLanguage(langUid: Long) {
        this.filterByLang = langUid
        fragmentViewContract.setContentEntryProvider(contentEntryDao.getChildrenByParentUidWithCategoryFilter(parentUid!!, filterByLang, filterByCategory, activeAccount?.personUid ?: 0))
    }

    @JsName("handleClickFilterByCategory")
    fun handleClickFilterByCategory(contentCategoryUid: Long) {
        this.filterByCategory = contentCategoryUid
        fragmentViewContract.setContentEntryProvider(contentEntryDao.getChildrenByParentUidWithCategoryFilter(parentUid!!, filterByLang, filterByCategory, activeAccount?.personUid ?: 0))
    }

    @JsName("handleUpNavigation")
    fun handleUpNavigation() {
        val impl = UstadMobileSystemImpl.instance
        impl.go(HomeView.VIEW_NAME, mapOf(), view.viewContext,
                UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)

    }

    @JsName("handleDownloadStatusButtonClicked")
    fun handleDownloadStatusButtonClicked(entry: ContentEntry) {
        UstadMobileSystemImpl.instance.go("DownloadDialog",
                mapOf("contentEntryUid" to entry.contentEntryUid.toString()), context)
    }

    companion object {

        @JsName("ARG_CONTENT_ENTRY_UID")
        const val ARG_CONTENT_ENTRY_UID = "entryid"

        const val ARG_NO_IFRAMES = "noiframe"

        const val ARG_DOWNLOADED_CONTENT = "downloaded"

        const val ARG_RECYCLED_CONTENT = "recycled"

        const val ARG_LIBRARIES_CONTENT = "libraries"
    }
}
