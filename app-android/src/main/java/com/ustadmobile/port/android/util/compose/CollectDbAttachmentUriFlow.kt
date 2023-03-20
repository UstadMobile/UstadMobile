package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.resolveAttachmentAndroidUri
import kotlinx.coroutines.flow.Flow

/**
 * Collect the uri of an attachment. This requires a flow from the database that emits a String.
 * The string will be passed to the resolveAttachmentAndroidUri
 *
 * @param key1 invalidation key (eg query parameter)
 * @param flowAdapter function that will return a flow emitting a String to pass to resolve attachment uri
 *
 * @return String of the resolved attachment uri
 */
@Composable
fun collectDbAttachmentUriFlow(
    key1: Any?,
    flowAdapter: (UmAppDatabase) -> Flow<String?>
) : String? {
    val db = rememberActiveDatabase(tag = DoorTag.TAG_DB)
    val attachmentUri = collectDbFlow(
        key1 = key1,
        initialValue = null,
        db = db,
        flowAdapter = flowAdapter
    )
    return remember(attachmentUri) {
        attachmentUri?.let { uri -> db?.resolveAttachmentAndroidUri(uri) }?.toString()
    }
}
