package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.util.systemTimeInMillis

/**
 * Represents the user granting permission for access to their data to an external app.
 *
 * This may be extended by a 1:many join to scopes in the future.
 */
@Entity
@kotlinx.serialization.Serializable
data class ExternalAppPermission(

    @PrimaryKey(autoGenerate = true)
    var eapUid: Int = 0,

    /**
     * The personUid the grant applies for
     */
    var eapPersonUid: Long = 0,

    /**
     * The UID of the caller app (android specific)
     */
    var eapPackageId: String? = null,

    /**
     * The start time of the grant
     */
    var eapStartTime: Long = 0,

    /**
     * The end time of the grant (if the end time is before the current time, it means the grant is
     * invalid)
     */
    var eapExpireTime: Long = 0,

    /**
     * Token that must be presented by the caller
     */
    var eapAuthToken: String? = null,

    /**
     * The accountname as
     */
    var eapAndroidAccountName: String? = null,
)

