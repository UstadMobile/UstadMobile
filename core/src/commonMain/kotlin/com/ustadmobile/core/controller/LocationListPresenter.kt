package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl

import com.ustadmobile.core.view.LocationListView
import com.ustadmobile.core.view.LocationDetailView

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Location

import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.lib.db.entities.LocationWithSubLocationCount

import com.ustadmobile.core.view.LocationDetailView.Companion.LOCATION_UID

/**
 * Presenter for LocationList view
 */
class LocationListPresenter(context: Any, arguments: Map<String, String>?,
                            view: LocationListView,
                            val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<LocationListView>(context, arguments!!, view) {

    private var umProvider: UmProvider<LocationWithSubLocationCount>? = null
    internal var repository: UmAppDatabase
    private val providerDao: LocationDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.locationDao


    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllLocationsWithCount()
        view.setListProvider(umProvider!!)

    }

    fun handleClickPrimaryActionButton() {
        val args = HashMap<String, String>()
        impl.go(LocationDetailView.VIEW_NAME, args, context)
    }

    fun handleClickEditLocation(uid: Long) {
        val args = HashMap<String, String>()
        args.put(LOCATION_UID, uid)
        impl.go(LocationDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteLocation(uid: Long) {
        providerDao.inactivateLocationAsync(uid, null!!)
    }

}
