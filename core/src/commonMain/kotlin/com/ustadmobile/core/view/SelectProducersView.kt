package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithInventory


/**
 * Core View. Screen is for SelectProducer's View
 */
interface SelectProducersView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateProducersOnView(producers: List<PersonWithInventory>)

    fun updateSpinner(presents: Array<String?>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectProducer"

        //Any argument keys:
        const val ARG_PRODUCER_UID = "ArgProducerUid"
    }


}

