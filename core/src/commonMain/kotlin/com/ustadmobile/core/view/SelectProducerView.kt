package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Person


/**
 * Core View. Screen is for SelectProducer's View
 */
interface SelectProducerView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DoorLiveData<Person?>)

    fun updateSpinner(presents: Array<String?>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectProducer"

        //Any argument keys:
        const val ARG_PRODUCER_UID = "ArgProducerUid"
    }


}

