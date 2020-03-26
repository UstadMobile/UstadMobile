package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class SelectMultipleEntriesTreeDialogPresenter(context: Any, arguments: Map<String, String?>,
                                               view: SelectMultipleEntriesTreeDialogView,
                                               private val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao) :
        CommonEntityHandlerPresenter<SelectMultipleEntriesTreeDialogView>(context, arguments, view) {

    var selectedEntriesList: MutableList<Long> = mutableListOf()

    var selectedOptions = mutableMapOf<String, Long>()

    var jobCount = 0

    init {
        val entryArray = arguments.getValue(SelectMultipleEntriesTreeDialogView.ARG_CONTENT_ENTRY_SET)
        val selectedEntriesListNM = entryArray!!.split(",").filter { it.isNotEmpty() }.map {
            it.trim().toLong()
        }
        selectedEntriesList = selectedEntriesListNM.toMutableList()
        getTopEntries()
    }

    private fun getTopEntries() {
        jobCount++
        view.showBaseProgressBar(jobCount > 0)
        GlobalScope.launch {
            val entriesList = contentEntryParentChildJoinDao.selectTopEntries()
            view.runOnUiThread(Runnable {
                view.populateTopEntries(entriesList)
                jobCount--
                view.showBaseProgressBar(jobCount > 0)
            })
        }
    }


    override fun entityChecked(entityName: String, entityUid: Long, checked: Boolean) {
        if (checked) {
            selectedOptions[entityName] = entityUid
            if(!selectedEntriesList.contains(entityUid)){
                selectedEntriesList.add(entityUid)
            }
        } else {
            selectedOptions.remove(entityName)
            if(selectedEntriesList.contains(entityUid)){
                selectedEntriesList.remove(entityUid)
            }
        }
    }

}