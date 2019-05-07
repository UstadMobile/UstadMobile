package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * Created by mike on 2/2/18.
 */

class EntryStatusResponseWithNode : EntryStatusResponse {

    @UmEmbedded
    var networkNode: NetworkNode? = null

    constructor()

    constructor(networkNode: NetworkNode) {
        this.networkNode = networkNode
    }
}
