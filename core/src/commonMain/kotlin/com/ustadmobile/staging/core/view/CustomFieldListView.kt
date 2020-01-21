package com.ustadmobile.core.view


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.CustomField

/**
 * Core View. Screen is for CustomDetailList's View
 */
interface CustomFieldListView : UstadView {

    //Any argument keys:

    fun setListProvider(provider: DataSource.Factory<Int, CustomField>)

    fun setEntityTypePresets(entityTypePresets: Array<String>)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "CustomFieldList"
    }


}

