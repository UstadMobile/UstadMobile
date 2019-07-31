package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class SelectMultipleEntriesTreeDialogPresenter(context: Any, arguments: Map<String, String?>, view: SelectMultipleEntriesTreeDialogView): CommonEntityHandlerPresenter<SelectMultipleEntriesTreeDialogView>(context, arguments, view) {

    var selectedEntriesList: List<Long> = listOf()

    var selectedOptions = mutableMapOf<String, Long>()

    var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {
        val entryArray = arguments.getValue(SelectMultipleEntriesTreeDialogView.ARG_CONTENT_ENTRY_SET)
        selectedEntriesList = entryArray!!.split(",").filter { it.isNotEmpty() }.map {
            it.trim().toLong()
        }
        getTopEntries()
    }

    private fun getTopEntries() {
        GlobalScope.launch {
            val entriesList = repository.contentEntryParentChildJoinDao.selectTopEntries()
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