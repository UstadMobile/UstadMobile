package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.port.android.view.util.CrudActivityResultContract
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

class @Entity@ActivityResultContract(context: Context) : CrudActivityResultContract<@Entity@>(context,
        @Entity@EditActivity::class.java, @Entity@ListActivity::class.java) {

    override fun parseResult(resultCode: Int, intent: Intent?): List<@Entity@>? {
        if(resultCode != Activity.RESULT_OK)
            return null

        val jsonStr = intent?.extras?.getString(RESULT_EXTRA_KEY) ?: return null
        return Json.parse(@Entity@.serializer().list, jsonStr)
    }

    companion object {

        const val RESULT_EXTRA_KEY = "@Entity@Result"

    }
}
