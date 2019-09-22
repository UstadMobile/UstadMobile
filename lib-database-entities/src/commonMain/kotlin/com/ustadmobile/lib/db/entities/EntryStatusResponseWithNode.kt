package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * Created by mike on 2/2/18.
 */
@Serializable
class EntryStatusResponseWithNode(@Embedded var networkNode: NetworkNode? = null) : EntryStatusResponse()
