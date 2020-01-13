package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.AuditLog
import com.ustadmobile.lib.db.entities.AuditLogWithNames


/**
 * Core View. Screen is for AuditLogList's View
 */
interface AuditLogListView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, AuditLogWithNames>)

    /**
     * Generate csv report from the View which has the table data
     */
    fun generateCSVReport(data: List<Array<String>>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "AuditLogList"
    }


}

