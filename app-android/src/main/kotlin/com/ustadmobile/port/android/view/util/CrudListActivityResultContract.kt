package com.ustadmobile.port.android.view.util

import android.content.Context
import android.content.Intent
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView

class CrudListActivityResultContract<T>(context: Context, entityClass: Class<T>,
                                        val listActivityClass: Class<*>)
    : AbstractCrudActivityResultContract<Map<String, String>, T>(context, entityClass) {

    override fun createIntent(input: Map<String, String>?) = Intent(context, listActivityClass).apply {
        putExtras((input ?: mapOf()).toBundle())
        putExtra(UstadView.ARG_LISTMODE, ListViewMode.PICKER.toString())
    }

}