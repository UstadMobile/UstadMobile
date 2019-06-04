package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

/**
 * Created by mike on 2/2/18.
 */

class EntryStatusResponseWithNode(@Embedded var networkNode: NetworkNode? = null) : EntryStatusResponse()
