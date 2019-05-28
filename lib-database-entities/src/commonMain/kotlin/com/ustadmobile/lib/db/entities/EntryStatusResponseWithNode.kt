package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * Created by mike on 2/2/18.
 */

class EntryStatusResponseWithNode : EntryStatusResponse {

    @UmEmbedded
    @Embedded
    var networkNode: NetworkNode? = null

    constructor(networkNode: NetworkNode) {
        this.networkNode = networkNode
    }
}
