package com.ustadmobile.port.android.util

import android.content.Context
import android.os.Bundle
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class DeleteTempFilesNavigationListener(context: Context) : NavController.OnDestinationChangedListener {

    val tmpFilesPref = context.getSharedPreferences(SHAREDPREF_TMPFILE_REG, Context.MODE_PRIVATE)

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val tmpFiles = tmpFilesPref.all.keys
        val filesToDelete = mutableSetOf<String>()
        tmpFiles.forEach {filePath ->
            val ownerDestinationId = tmpFilesPref.getInt(filePath, -1)
            try {
                controller.getBackStackEntry(ownerDestinationId)
            }catch(e: IllegalArgumentException) {
                //destination is no longer on backstack - remove the file
                filesToDelete += filePath
            }
        }

        if(tmpFiles.isEmpty())
            return


        GlobalScope.launch(Dispatchers.IO) {
            filesToDelete.forEach {
                try {
                    File(it).deleteRecursively()
                    Napier.d("Delete destination registered tmp file: $it")
                    tmpFilesPref.edit {
                        remove(it)
                    }
                }catch(e: Exception) {
                    Napier.e("Could not delete registered tmp file", e)
                }
            }
        }
    }

    companion object {

        const val SHAREDPREF_TMPFILE_REG = "TMPFILEREG"

    }
}

