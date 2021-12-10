package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry

/**
 * Create a deep link for the given ContentEntry. This will use the ContentEntryDetailView for
 * an item that is a leaf, or ContentEntryListView if the item is a branch
 */
fun ContentEntry.toDeepLink(endpoint: Endpoint) : String {
    return makeContentEntryDeepLink(contentEntryUid, leaf, endpoint)
}

fun makeContentEntryDeepLink(contentEntryUid: Long, leaf: Boolean, endpoint: Endpoint): String {
    val viewName = if(leaf) {
        ContentEntryDetailView.VIEW_NAME
    }else {
        ContentEntryList2View.VIEW_NAME
    }

    return mapOf(UstadView.ARG_ENTITY_UID to contentEntryUid.toString()).toDeepLink(
        endpoint.url, viewName)
}