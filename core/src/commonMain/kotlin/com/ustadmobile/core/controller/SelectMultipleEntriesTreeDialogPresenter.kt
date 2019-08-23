package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class SelectMultipleEntriesTreeDialogPresenter(context: Any, arguments: Map<String, String?>,
                                               view: SelectMultipleEntriesTreeDialogView,
                                               private val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao):
        CommonEntityHandlerPresenter<SelectMultipleEntriesTreeDialogView>(context, arguments, view) {

    var selectedEntriesList: List<Long> = listOf()

    var selectedOptions = mutableMapOf<String, Long>()

    init {
        val entryArray = arguments.getValue(SelectMultipleEntriesTreeDialogView.ARG_CONTENT_ENTRY_SET)
        selectedEntriesList = entryArray!!.split(",").filter { it.isNotEmpty() }.map {
            it.trim().toLong()
        }
        getTopEntries()
    }

    private fun getTopEntries() {
        GlobalScope.launch {
            val entriesList = contentEntryParentChildJoinDao.selectTopEntries()
            view.runOnUiThread(Runnable {
                view.populateTopEntries(entriesList)
            })
        }
    }


    override fun entityChecked(entityName: String, entityUid: Long, checked: Boolean) {
        if (checked) {
            selectedOptions[entityName] = entityUid
        } else {
            selectedOptions.remove(entityName)
        }
    }

}