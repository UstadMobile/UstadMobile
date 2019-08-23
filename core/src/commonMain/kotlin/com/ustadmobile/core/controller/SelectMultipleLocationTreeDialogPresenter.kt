package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.Companion.ARG_LOCATIONS_SET
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


class SelectMultipleLocationTreeDialogPresenter(context: Any, arguments: Map<String, String?>,
                                                view: SelectMultipleLocationTreeDialogView,
                                                private val locationDao: LocationDao)
    : CommonEntityHandlerPresenter<SelectMultipleLocationTreeDialogView>(context, arguments, view) {

    var selectedLocationsList: List<Long> = listOf()

    var selectedOptions = mutableMapOf<String, Long>()

    init {
        val locationsArray = arguments.getValue(ARG_LOCATIONS_SET)
        selectedLocationsList = locationsArray!!.split(",").filter { it.isNotEmpty() }.map {
            it.trim().toLong()
        }
        getTopLocations()
    }

    override fun entityChecked(entityName: String, entityUid: Long, checked: Boolean) {
        if (checked) {
            selectedOptions[entityName] = entityUid
        } else {
            selectedOptions.remove(entityName)
        }
    }

    /**
     * Gets top locations and load initial data to the recycler view
     */
    private fun getTopLocations() {
        GlobalScope.launch {
            val locationList = locationDao.findTopLocationsAsync()
            view.runOnUiThread(Runnable {
                view.populateTopLocation(locationList)
            })
        }
    }

}