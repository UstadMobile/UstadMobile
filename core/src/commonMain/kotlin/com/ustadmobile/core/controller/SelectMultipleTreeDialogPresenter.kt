package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.SelectMultipleTreeDialogView
import com.ustadmobile.lib.db.entities.Location

import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATIONS_SET


/**
 * The SelectMultipleTreeDialog Presenter.
 */
class SelectMultipleTreeDialogPresenter(context: Any, arguments: Map<String, String>?,
                                        view: SelectMultipleTreeDialogView) :
        CommonLocationHandlerPresenter<SelectMultipleTreeDialogView>(context, arguments!!, view) {

    var selectedOptions: HashMap<String, Long>
        internal set

    var selectedLocationsList: List<Long>? = null
        get() = if (field == null) {
            ArrayList()
        } else field

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        if (arguments!!.containsKey(ARG_LOCATIONS_SET)) {
            val locationsArray = arguments!!.get(ARG_LOCATIONS_SET) as LongArray
            this.selectedLocationsList = ReportOverallAttendancePresenter.convertLongArray(locationsArray)
        }

        selectedOptions = HashMap()
        getTopLocations()

    }

    fun getTopLocations() {
        val locationDao = repository.locationDao
        locationDao.findTopLocationsAsync(object : UmCallback<List<Location>> {
            override fun onSuccess(result: List<Location>?) {
                view.populateTopLocation(result!!)
            }

            override fun onFailure(exception: Throwable?) {

            }
        })
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }


    fun handleClickPrimaryActionButton() {
        view.finish()
    }

    override fun locationChecked(locationName: String, locationUid: Long?, checked: Boolean) {
        if (checked) {
            selectedOptions[locationName] = locationUid
        } else {
            if (selectedOptions.containsKey(locationName)) {
                selectedOptions.remove(locationName)
            }
        }
    }

}
