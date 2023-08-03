package com.ustadmobile.port.android.util.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.resolveAttachmentAndroidUri

/**
 * Remember the resolved attachment uri
 *
 * @param attachmentUri an attachment URI (as per the uri field on an entity)
 * @param db the database to use to resolve attachment - nullable to avoid crashing previews etc
 *
 * @return Uri
 */
@Composable
fun rememberResolvedAttachmentUri(
    attachmentUri: String?,
    db: UmAppDatabase? = rememberActiveDatabase()
): Uri? {
    return remember(attachmentUri) {
        attachmentUri?.let { uri ->  db?.resolveAttachmentAndroidUri(uri)}
    }
}
