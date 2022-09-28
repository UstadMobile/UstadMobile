package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.PersonParentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID, tracker = PersonParentJoinReplicate::class)
@Triggers(arrayOf(
     Trigger(
         name = "personparentjoin_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         sqlStatements = [
             """REPLACE INTO PersonParentJoin(ppjUid, ppjPcsn, ppjLcsn, ppjLcb, ppjLct, ppjParentPersonUid, ppjMinorPersonUid, ppjRelationship, ppjEmail, ppjPhone, ppjInactive, ppjStatus, ppjApprovalTiemstamp, ppjApprovalIpAddr) 
             VALUES (NEW.ppjUid, NEW.ppjPcsn, NEW.ppjLcsn, NEW.ppjLcb, NEW.ppjLct, NEW.ppjParentPersonUid, NEW.ppjMinorPersonUid, NEW.ppjRelationship, NEW.ppjEmail, NEW.ppjPhone, NEW.ppjInactive, NEW.ppjStatus, NEW.ppjApprovalTiemstamp, NEW.ppjApprovalIpAddr) 
             /*psql ON CONFLICT (ppjUid) DO UPDATE 
             SET ppjPcsn = EXCLUDED.ppjPcsn, ppjLcsn = EXCLUDED.ppjLcsn, ppjLcb = EXCLUDED.ppjLcb, ppjLct = EXCLUDED.ppjLct, ppjParentPersonUid = EXCLUDED.ppjParentPersonUid, ppjMinorPersonUid = EXCLUDED.ppjMinorPersonUid, ppjRelationship = EXCLUDED.ppjRelationship, ppjEmail = EXCLUDED.ppjEmail, ppjPhone = EXCLUDED.ppjPhone, ppjInactive = EXCLUDED.ppjInactive, ppjStatus = EXCLUDED.ppjStatus, ppjApprovalTiemstamp = EXCLUDED.ppjApprovalTiemstamp, ppjApprovalIpAddr = EXCLUDED.ppjApprovalIpAddr
             */"""
         ]
     )
))
open class PersonParentJoin {

    @PrimaryKey(autoGenerate = true)
    var ppjUid: Long = 0

    @MasterChangeSeqNum
    var ppjPcsn: Long = 0

    @LocalChangeSeqNum
    var ppjLcsn: Long = 0

    @LastChangedBy
    var ppjLcb: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var ppjLct: Long = 0

    /**
     * The personUid of the parent or legal guardian
     */
    var ppjParentPersonUid: Long = 0

    /**
     * The personUid of the minor (e.g. the child)
     */
    var ppjMinorPersonUid: Long = 0

    /**
     * The relationship type e.g. mother, father, other legal guardian
     */
    var ppjRelationship: Int = 0

    var ppjEmail: String? = null

    var ppjPhone: String? = null

    var ppjInactive: Boolean = false

    var ppjStatus: Int = 0

    var ppjApprovalTiemstamp: Long = 0

    var ppjApprovalIpAddr: String? = null

    companion object {

        const val TABLE_ID = 512

        const val STATUS_UNSET = 0

        const val STATUS_APPROVED = 1

        const val STATUS_REJECTED = 2

        const val RELATIONSHIP_MOTHER = 1

        const val RELATIONSHIP_FATHER = 2

        const val RELATIONSHIP_OTHER = 4
    }

}