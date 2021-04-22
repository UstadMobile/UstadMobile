package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.NotificationSetting.Companion.TABLE_ID

/**
 * This entity represents a Notification Setting by a particular user.
 */
@Entity
@SyncableEntity(tableId = TABLE_ID)
open class NotificationSetting {

    @PrimaryKey(autoGenerate = true)
    var nsUid: Long = 0

    @LastChangedBy
    var nsLcb: Int = 0

    @MasterChangeSeqNum
    var nsPcsn: Long = 0

    @LocalChangeSeqNum
    var nsLcsn: Long = 0

    var nsPersonUid: Long = 0

    //The type of notification as per the TYPE_ constants
    var nsType: Int = 0

    //Reserved for future use: allow the user to select a particular entity e.g. alerts for a
    // particular class. This flag represents the scope tableId (e.g. school)
    var nsEntityFilterScope: Int = 0

    //Reserved for future use: allow the user to select a particular entity e.g. alerts for a
    // particular class. This flag represents the scope entity uid ( e.g. schoolUid where filterscope = SCHOOL)
    var nsEntityFilterUid: Long = 0

    //Reserved for future use: the channel by which this notification should be delivered.
    var nsChannel: Int = CHANNEL_APP

    //Where extra params can be stored as JSON Map<String, String>
    var nsParams: String? = null

    var nsThreshold: Float = 0f

    companion object {

        const val TABLE_ID = 271

        const val TYPE_TAKE_ATTENDANCE_REMINDER = 1

        const val TYPE_ATTENDANCE_MISSED_NOTIFICATION = 2


        const val CHANNEL_APP = 1

        const val CHANNEL_EMAIL = 2

    }

}