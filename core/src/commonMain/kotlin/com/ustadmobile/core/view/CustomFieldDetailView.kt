package com.ustadmobile.core.view


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValueOption

/**
 * Core View. Screen is for CustomDetailDetail's View
 */
interface CustomFieldDetailView : UstadView {

    fun setDropdownPresetsOnView(dropdownPresets: Array<String>)
    fun setEntityTypePresetsOnView(entityTypePresets: Array<String>)

    fun setCustomFieldOnView(customField: CustomField)

    fun showOptions(show: Boolean)

    fun setListProvider(listProvider: DataSource.Factory<Int, CustomFieldValueOption>)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "CustomDetailDetail"

        //Any argument keys:
        val ARG_CUSTOM_FIELD_UID = "CustomFieldUid"
    }


}

