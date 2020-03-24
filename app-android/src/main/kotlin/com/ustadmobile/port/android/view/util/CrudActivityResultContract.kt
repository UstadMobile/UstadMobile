package com.ustadmobile.port.android.view.util

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.GetResultMode
import com.ustadmobile.core.view.UstadView

/**
 * This defines a basic ActivityResultContract that is used for returning results from activities
 * when picking linked entities.
 *
 * @param editActivityClass - The activity that provides the edit view. Used when creating a new entity
 * @param listActivityClass - The activity that provides a list view (e.g. thin wrapper around the ListFragment)
 */
abstract class CrudActivityResultContract<T>(val context: Context,
    val editActivityClass: Class<*>,
    val listActivityClass: Class<*>) : ActivityResultContract<Map<String, String>, List<T>?>() {

    override fun createIntent(input: Map<String, String>): Intent {
        val getResultModeStr = input[UstadView.ARG_GETRESULTMODE]
        val activityClass = if(getResultModeStr != null && GetResultMode.valueOf(getResultModeStr) == GetResultMode.CREATENEW) {
            editActivityClass
        }else {
            listActivityClass
        }

        return Intent(context, activityClass).apply {
            putExtras(input.toBundle())
        }
    }

}