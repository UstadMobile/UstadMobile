package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.lib.db.entities.Location
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


class SelectMultipleLocationTreeDialogPresenter(context: Any, arguments: Map<String, String?>,
                                                view: SelectMultipleLocationTreeDialogView,
                                                private val locationDao: LocationDao)
    : CommonEntityHandlerPresenter<SelectMultipleLocationTreeDialogView>(context, arguments, view) {

    var selectedLocationsList: MutableList<Long> = mutableListOf()

    var selectedOptions = mutableMapOf<String, Long>()

    init {
        if(arguments.containsKey(ARG_LOCATIONS_SET)){
            val locationsArray = arguments.getValue(ARG_LOCATIONS_SET)
            val selectedLocationsListNM = locationsArray!!.split(",").filter { it.isNotEmpty() }.map {
                it.trim().toLong()
            }
            selectedLocationsList = selectedLocationsListNM.toMutableList()
        }

        getTopLocations()
    }

    override fun entityChecked(entityName: String, entityUid: Long, checked: Boolean) {
        if (checked) {
            selectedOptions[entityName] = entityUid
            if(!selectedLocationsList.contains(entityUid)){
                selectedLocationsList.add(entityUid)
            }
        } else {
            selectedOptions.remove(entityName)
            if(selectedLocationsList.contains(entityUid)){
                selectedLocationsList.remove(entityUid)
            }
        }

    }

    private fun handleGetTopLocations(locations:List<Location>?){
        view.runOnUiThread(Runnable {
            if(locations!=null) {
                view.populateTopLocation(locations)
            }
        })
    }
    /**
     * Gets top locations and load initial data to the recycler view
     */
    private fun getTopLocations() {
        val thisP=this
        GlobalScope.launch {
            val locationListLive = locationDao.findTopLocationsLive()
            view.runOnUiThread(Runnable {
                locationListLive.observeWithPresenter(thisP, thisP::handleGetTopLocations)
            })
        }
    }

}