package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.Companion.ARG_LOCATIONS_SET
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The SelectMultipleTreeDialog Presenter.
 */
class SelectMultipleLocationTreeDialogPresenter(context: Any, arguments: Map<String, String?>,
                                                view: SelectMultipleLocationTreeDialogView)
    : CommonEntityHandlerPresenter<SelectMultipleLocationTreeDialogView>(context, arguments, view) {

    /**
     * Getter for selected Locations
     * @return  selected options (locations) as a HashMap<Location name, Location Uid>
    </Location> */
    val selectedOptions: HashMap<String, Long>

    private var selectedLocationsList: List<Long>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        if (arguments.containsKey(ARG_LOCATIONS_SET)) {
            val locationsArrayString = arguments.get(ARG_LOCATIONS_SET).toString()

            selectedLocationsList = convertCSVStringToLongList(locationsArrayString)

        }
        selectedOptions = HashMap()

        //Get top locations - and populate the view with it.
        getTopLocations()

    }


    /**
     * Gets top locations and load initial data to the recycler view
     */
    private fun getTopLocations() {
        val locationDao = repository.locationDao
        GlobalScope.launch {
            val result = locationDao.findTopLocationsAsync()
            view.populateTopLocation(result)
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
    }

    fun getSelectedLocationsList(): List<Long> {
        return (if (selectedLocationsList == null) {
            ArrayList()
        } else selectedLocationsList)!!
    }

    override fun entityChecked(entityName: String, entityUid: Long?, checked: Boolean) {
        if (checked) {
            if (entityUid != null) {
                selectedOptions.put(entityName, entityUid)
            }
        } else {
            selectedOptions.remove(entityName)
        }
    }

    fun convertCSVStringToLongList(csString:String):List<Long> {
        val list = ArrayList<Long>()
        for (s in csString.split((",").
                toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()){
            val p = s.trim()
            list.add(p as Long)
        }

        return list
    }

}
