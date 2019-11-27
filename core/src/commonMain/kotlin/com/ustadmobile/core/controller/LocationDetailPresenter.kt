package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.LocationDetailView
import com.ustadmobile.core.view.LocationDetailView.Companion.LOCATIONS_SET
import com.ustadmobile.core.view.LocationDetailView.Companion.LOCATION_UID
import com.ustadmobile.lib.db.entities.Location
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * Presenter for LocationDetail view
 */
class LocationDetailPresenter(context: Any, arguments: Map<String, String>?, view: LocationDetailView)
    : CommonLocationHandlerPresenter<LocationDetailView>(context, arguments!!, view) {

    internal var currentLocation: Location? = null
    internal var updatedLocation: Location? = null
    private var currentLocationUid: Long = 0

    internal var repository: UmAppDatabase
    internal var locationDao: LocationDao

    internal var selectedOptions: HashMap<String, Long>

    private var selectedLocationsList: ArrayList<Long>? = null

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        locationDao = repository.locationDao

        if (arguments!!.containsKey(LOCATIONS_SET)) {
            val locationsArray = arguments!!.get(LOCATIONS_SET) as LongArray
            selectedLocationsList = ReportOverallAttendancePresenter.convertLongArray(locationsArray)
        }

        if (arguments!!.containsKey(LOCATION_UID)) {
            currentLocationUid = arguments!!.get(LOCATION_UID)!!.toLong()
        }

        selectedOptions = HashMap()
        getTopLocations()

    }

    private fun getTopLocations() {
        val locationDao = repository.locationDao
        GlobalScope.launch {
            val result = locationDao.findTopLocationsAsync()
            view.runOnUiThread(Runnable {
                view.populateTopLocation(result!!)
            })
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (currentLocationUid == 0L) {
            currentLocation = Location()
            currentLocation!!.title = ""
            currentLocation!!.locationActive = (false)

            GlobalScope.launch {
                val result = locationDao.insertAsync(currentLocation!!)
                initFromLocation(result!!)
            }
        } else {
            initFromLocation(currentLocationUid)
        }
    }

    private fun initFromLocation(locationUid: Long) {
        this.currentLocationUid = locationUid

        val locationUmLiveData = locationDao.findByUidLive(currentLocationUid)
        view.runOnUiThread(Runnable {
            locationUmLiveData.observe(this, this::handleLocationChanged)
        })

        GlobalScope.launch {
            val result = locationDao.findByUidAsync(locationUid)
            updatedLocation = result
            view.runOnUiThread(Runnable {
                view.updateLocationOnView(updatedLocation!!)
            })
        }

    }

    private fun handleLocationChanged(changedLocation: Location?) {
        if (currentLocation == null) {
            currentLocation = changedLocation
        }

        if (updatedLocation == null || updatedLocation != changedLocation) {
            updatedLocation = changedLocation
            selectedLocationsList = ArrayList()
            val parentLocationUid = updatedLocation!!.parentLocationUid
            selectedLocationsList!!.add(parentLocationUid)

            getTopLocations()

            view.runOnUiThread(Runnable {
                view.updateLocationOnView(updatedLocation!!)
            })

        }
    }

    fun handleClickDone() {
        selectedLocationsList = ArrayList(selectedOptions.values)

        var firstLocation: Long? = 0L
        if (!selectedLocationsList!!.isEmpty()) {
            firstLocation = selectedLocationsList!![0]
        }
        updatedLocation!!.parentLocationUid = firstLocation!!
        updatedLocation!!.locationActive = (true)

        GlobalScope.launch {
            locationDao.updateAsync(updatedLocation!!)
            view.finish()
        }
    }

    fun getSelectedLocationsList(): ArrayList<Long>? {
        return if (selectedLocationsList == null) {
            ArrayList()
        } else selectedLocationsList
    }

    fun setSelectedLocationsList(selectedLocationsList: ArrayList<Long>) {
        this.selectedLocationsList = selectedLocationsList
    }

    override fun locationChecked(locationName: String, locationUid: Long?, checked: Boolean) {
        if (checked) {
            selectedOptions[locationName] = locationUid!!
        } else {
            if (selectedOptions.containsKey(locationName)) {
                selectedOptions.remove(locationName)
            }
        }
    }

    fun updateLocationTitle(toString: String) {
        updatedLocation!!.title = toString
    }
}

