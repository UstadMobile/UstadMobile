package com.ustadmobile.core.util.ext

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gets the file name for a Uri (e.g. file) that has just been selected. When a file is selected
 * using ActivityResultContracts.GetContent the Uri that is returned might or might not have the
 * actual filename in the path. Therefor we run a query to try and get the specified DISPLAY_NAME,
 * and only if that doesn't return a result, then we fallback to using substringAfterLast("/").
 *
 * See:
 * https://developer.android.com/training/secure-file-sharing/retrieve-info
 *
 * @param uri the Uri of a file that has just been selected
 */
suspend fun ContentResolver.getFileNameAndSize(uri: Uri): Pair<String, Long> {
    return withContext(Dispatchers.IO) {
        query(uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null, null, null
        )?.use { cursor ->
            cursor.takeIf { cursor.moveToFirst() }?.let {
                Pair(cursor.getString(0), cursor.getLong(1))
            }
        } ?: Pair(uri.path?.substringAfterLast("/") ?: uri.toString(), -1)
    }
}
