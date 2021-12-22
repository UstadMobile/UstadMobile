package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = ClazzContentJoinTracker::class)
/*
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = [
            """
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
               $TABLE_ID AS tableId 
          FROM ChangeLog 
                JOIN ClazzContentJoin
                     ON ChangeLog.chTableId = $TABLE_ID 
                            AND ClazzContentJoin.ccjUid = ChangeLog.chEntityPk
                JOIN Clazz 
                    ON Clazz.clazzUid = ClazzContentJoin.ccjClazzUid                
                ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_CONTENT_SELECT}
                    ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
        """
        ],
        syncFindAllQuery = """
        SELECT ClazzContentJoin.* 
          FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_CONTENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ClazzContentJoin    
                    ON Clazz.clazzUid = ClazzContentJoin.ccjClazzUid  
         WHERE UserSession.usClientNodeId = :clientId 
           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    """
)

 */
@Serializable
class ClazzContentJoin  {

    @PrimaryKey(autoGenerate = true)
    var ccjUid: Long = 0

    @ColumnInfo(index = true)
    var ccjContentEntryUid: Long = 0

    var ccjClazzUid: Long = 0

    var ccjActive: Boolean = true

    @LocalChangeSeqNum
    var ccjLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var ccjMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var ccjLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var ccjLct: Long = 0

    companion object {

        const val TABLE_ID = 134

    }

}