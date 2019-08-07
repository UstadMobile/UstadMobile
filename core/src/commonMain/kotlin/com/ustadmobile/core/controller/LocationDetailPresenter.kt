package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.LocationDetailView
import com.ustadmobile.lib.db.entities.Location

import com.ustadmobile.core.view.LocationDetailView.Companion.LOCATIONS_SET
import com.ustadmobile.core.view.LocationDetailView.Companion.LOCATION_UID


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

    private var selectedLocationsList: MutableList<Long>? = null

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        locationDao = repository.locationDao

        if (arguments!!.containsKey(LOCATIONS_SET)) {
            val locationsArray = arguments!!.get(LOCATIONS_SET) as LongArray
            selectedLocationsList = ReportOverallAttendancePresenter.convertLongArray(locationsArray)
        }

        if (arguments!!.containsKey(LOCATION_UID)) {
            currentLocationUid = arguments!!.get(LOCATION_UID)
        }

        selectedOptions = HashMap()
        getTopLocations()

    }

    private fun getTopLocations() {
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

        if (currentLocationUid == 0L) {
            currentLocation = Location()
            currentLocation!!.title = ""
            currentLocation!!.setLocationActive(false)

            locationDao.insertAsync(currentLocation!!, object : UmCallback<Long> {
                override fun onSuccess(result: Long?) {
                    initFromLocation(result!!)
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        } else {
            initFromLocation(currentLocationUid)
        }
    }

    private fun initFromLocation(locationUid: Long) {
        this.currentLocationUid = locationUid

        val locationUmLiveData = locationDao.findByUidLive(currentLocationUid)
        locationUmLiveData.observe(this@LocationDetailPresenter,
                UmObserver<Location> { this@LocationDetailPresenter.handleLocationChanged(it) })

        locationDao.findByUidAsync(locationUid, object : UmCallback<Location> {
            override fun onSuccess(result: Location?) {
                updatedLocation = result
                view.updateLocationOnView(updatedLocation!!)
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }

    private fun handleLocationChanged(changedLocation: Location) {
        if (currentLocation == null) {
            currentLocation = changedLocation
        }

        if (updatedLocation == null || updatedLocation != changedLocation) {
            updatedLocation = changedLocation
            selectedLocationsList = ArrayList()
            val parentLocationUid = updatedLocation!!.parentLocationUid
            selectedLocationsList!!.add(parentLocationUid)

            getTopLocations()

            view.updateLocationOnView(updatedLocation!!)

        }
    }

    fun handleClickDone() {
        selectedLocationsList = ArrayList(selectedOptions.values)

        var firstLocation: Long? = 0L
        if (!selectedLocationsList!!.isEmpty()) {
            firstLocation = selectedLocationsList!![0]
        }
        updatedLocation!!.parentLocationUid = firstLocation
        updatedLocation!!.setLocationActive(true)

        locationDao.updateAsync(updatedLocation!!, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    fun getSelectedLocationsList(): List<Long> {
        return if (selectedLocationsList == null) {
            ArrayList()
        } else selectedLocationsList
    }

    fun setSelectedLocationsList(selectedLocationsList: MutableList<Long>) {
        this.selectedLocationsList = selectedLocationsList
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

    fun updateLocationTitle(toString: String) {
        updatedLocation!!.title = toString
    }
}

