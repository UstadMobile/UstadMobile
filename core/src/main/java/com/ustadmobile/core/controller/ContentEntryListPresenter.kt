package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.DummyView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language
import java.util.*

class ContentEntryListPresenter(context: Any, arguments: Map<String, String>?, private val viewContract: ContentEntryListView)
    : UstadBaseController<ContentEntryListView>(context, arguments!!, viewContract) {

    private var contentEntryDao: ContentEntryDao? = null

    private var filterByLang: Long = 0

    private var filterByCategory: Long = 0

    private var parentUid: Long? = null

    override fun onCreate(savedState: Map<String, String?> ?) {
        super.onCreate(savedState)
        val appDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
        contentEntryDao = appDatabase.contentEntryDao

        if (arguments.containsKey(ARG_CONTENT_ENTRY_UID)) {
            showContentByParent()
        } else if (arguments.containsKey(ARG_DOWNLOADED_CONTENT)) {
            showDownloadedContent()
        }
    }

    private fun showContentByParent() {
        parentUid = java.lang.Long.valueOf(arguments.get(ARG_CONTENT_ENTRY_UID))
        viewContract.setContentEntryProvider(contentEntryDao!!.getChildrenByParentUidWithCategoryFilter(parentUid!!, 0, 0))
        contentEntryDao!!.getContentByUuid(parentUid!!, object : UmCallback<ContentEntry> {
            override fun onSuccess(result: ContentEntry?) {
                if (result == null) {
                    viewContract.runOnUiThread(Runnable { viewContract.showError() })
                    return
                }
                viewContract.setToolbarTitle(result.title)
            }

            override fun onFailure(exception: Throwable) {
                viewContract.runOnUiThread(Runnable { viewContract.showError() })
            }
        })

        contentEntryDao!!.findUniqueLanguagesInList(parentUid!!, object : UmCallback<List<Language>> {
            override fun onSuccess(result: List<Language>?) {
                val languages = LinkedList(result)
                if (languages.size > 1) {
                    val selectLang = Language()
                    selectLang.name = "Language"
                    selectLang.langUid = 0
                    languages.add(0, selectLang)

                    val allLang = Language()
                    allLang.name = "All"
                    allLang.langUid = 0
                    languages.add(1, allLang)

                    viewContract.setLanguageOptions(result!!)
                }
            }

            override fun onFailure(exception: Throwable) {

            }
        })

        contentEntryDao!!.findListOfCategories(parentUid!!, object : UmCallback<List<DistinctCategorySchema>> {
            override fun onSuccess(result: List<DistinctCategorySchema>?) {
                if (result != null && !result.isEmpty()) {

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
                }

            }

            override fun onFailure(exception: Throwable) {

            }
        })
    }

    private fun showDownloadedContent() {
        viewContract.setContentEntryProvider(contentEntryDao!!.downloadedRootItems)
    }


    fun handleContentEntryClicked(entry: ContentEntry) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        val entryUid = entry.contentEntryUid

        contentEntryDao!!.findByUid(entryUid, object : UmCallback<ContentEntry> {
            override fun onSuccess(result: ContentEntry?) {
                if (result == null) {
                    viewContract.runOnUiThread(Runnable { viewContract.showError() })
                    return
                }

                if (result.isLeaf) {
                    args[ARG_CONTENT_ENTRY_UID] = entryUid.toString()
                    impl.go(ContentEntryDetailView.VIEW_NAME, args, view.context)
                } else {
                    args[ARG_CONTENT_ENTRY_UID] = entryUid.toString()
                    impl.go(ContentEntryListView.VIEW_NAME, args, view.context)
                }
            }

            override fun onFailure(exception: Throwable) {
                viewContract.runOnUiThread(Runnable { viewContract.showError() })
            }
        })
    }

    fun handleClickFilterByLanguage(langUid: Long) {
        this.filterByLang = langUid
        viewContract.setContentEntryProvider(contentEntryDao!!.getChildrenByParentUidWithCategoryFilter(parentUid!!, filterByLang, filterByCategory))
    }

    fun handleClickFilterByCategory(contentCategoryUid: Long) {
        this.filterByCategory = contentCategoryUid
        viewContract.setContentEntryProvider(contentEntryDao!!.getChildrenByParentUidWithCategoryFilter(parentUid!!, filterByLang, filterByCategory))
    }

    fun handleUpNavigation() {
        val impl = UstadMobileSystemImpl.instance
        impl.go(DummyView.VIEW_NAME, mapOf(), view.context,
                UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP or UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP)

    }

    fun handleDownloadStatusButtonClicked(entry: ContentEntry) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args["contentEntryUid"] = entry.contentEntryUid.toString()
        impl.go("DownloadDialog", args, context)
    }

    companion object {

        const val ARG_CONTENT_ENTRY_UID = "entryid"

        const val ARG_DOWNLOADED_CONTENT = "downloaded"
    }
}
