package com.ustadmobile.port.android.util.ext

import android.content.Context
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.port.android.util.DeleteTempFilesNavigationListener.Companion.SHAREDPREF_TMPFILE_REG
import java.io.File

fun NavController.currentBackStackEntrySavedStateMap() = this.currentBackStackEntry?.savedStateHandle?.toStringMap()

/**
 * Register a given file that should be deleted once the given destination has been popped from the
 * back stack.
 *
 * @param context Context to use to find the cache dir and access shared preferences.
 * @param file File or directory that should be deleted when the given destination is popped from the backstack.
 * @param destination the destination, which when popped from the backstack, should trigger the given
 * file to be deleted.
 */
fun NavController.registerDestinationTempFile(context: Context, file: File,
                                              destination: NavDestination? = this.currentDestination) {
    val destinationId = destination?.id ?: return
    context.getSharedPreferences(SHAREDPREF_TMPFILE_REG, Context.MODE_PRIVATE).edit {
        putInt(file.absolutePath, destinationId)
    }
}

/**
 * Create a temp file that will be deleted once the given destination has been popped from the back
 * stack. This will save an entry to a sharedpreference key in a dedicated SharedPreferences.
 * DeleteTempNavigationListener will delete any temporary files for any destination that is no longer
 * in the backstack.
 *
 * @param context Context to use to find the cache dir and access shared preferences
 * @param name filename to create (under context.cacheDir)
 * @param destination the destination with which this temporary file will be associated.
 */
fun NavController.createTempFileForDestination(context: Context, name: String,
                                               destination: NavDestination? = this.currentDestination): File {
    val newTmpFile = File(context.cacheDir, name)
    registerDestinationTempFile(context, newTmpFile, destination)
    return newTmpFile
}

/**
 * Create a temporary directory for the given destination. This is the smae as createTempFileForDestination,
 * only it creates a directory instead of a file.
 *
 * Because DeleteTempNavigationListener will use deleteRecursively, anything in the directory will
 * be deleted once the destination is no longer in the stack
 */
fun NavController.createTempDirForDestination(context: Context, name: String,
                                              destination: NavDestination? = this.currentDestination): File {
    val newTmpDir = File(context.cacheDir, name)
    newTmpDir.mkdirs()
    registerDestinationTempFile(context, newTmpDir, destination)
    return newTmpDir
}
