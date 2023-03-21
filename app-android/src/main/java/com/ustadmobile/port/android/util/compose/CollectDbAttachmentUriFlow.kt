package com.ustadmobile.port.android.util.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.resolveAttachmentAndroidUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Collect the uri of an attachment entity. This requires a flow of the entity, and an adapter
 * block that will get the Uri field of the entity with attachment. The flow will then be mapped
 * through resolveAttachmentAndroidUri
 *
 * @param db The database to use to resolve the attachment uri
 * @param uriFieldBlock function to return the entity's attachment uri field e.g.
 *  { myEntity.attachmentUriField }
 *
 * @receiver Flow (e.g. flow of an entity from the database, or any other flow)
 */
@Composable
fun <T> Flow<T>?.collectAttachmentUri(
    db: UmAppDatabase?,
    uriFieldBlock: (T) -> String?
): State<Uri?> {
    val uriFlow = remember(key1 = this) {
        if(this != null) {
            map(uriFieldBlock).map { uriFieldStr ->
                uriFieldStr?.let { uriField -> db?.resolveAttachmentAndroidUri(uriField) }
            }
        }else {
            flowOf(null)
        }
    }

    return uriFlow.collectAsState(initial = null)
}

