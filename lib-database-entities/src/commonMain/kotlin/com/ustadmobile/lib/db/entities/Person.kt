package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.Person.Companion.TABLE_ID
import kotlinx.serialization.Serializable

/**
 * Created by mike on 3/8/18.
 */

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = PersonReplicate::class)
 @Triggers(arrayOf(
     Trigger(
         name = "person_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         sqlStatements = [
             """REPLACE INTO Person(personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personNotes, fatherName, fatherNumber, motherName, motherNum, dateOfBirth, personAddress, personOrgId, personGroupUid, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy, personLct, personCountry, personType) 
             VALUES (NEW.personUid, NEW.username, NEW.firstNames, NEW.lastName, NEW.emailAddr, NEW.phoneNum, NEW.gender, NEW.active, NEW.admin, NEW.personNotes, NEW.fatherName, NEW.fatherNumber, NEW.motherName, NEW.motherNum, NEW.dateOfBirth, NEW.personAddress, NEW.personOrgId, NEW.personGroupUid, NEW.personMasterChangeSeqNum, NEW.personLocalChangeSeqNum, NEW.personLastChangedBy, NEW.personLct, NEW.personCountry, NEW.personType) 
             /*psql ON CONFLICT (personUid) DO UPDATE 
             SET username = EXCLUDED.username, firstNames = EXCLUDED.firstNames, lastName = EXCLUDED.lastName, emailAddr = EXCLUDED.emailAddr, phoneNum = EXCLUDED.phoneNum, gender = EXCLUDED.gender, active = EXCLUDED.active, admin = EXCLUDED.admin, personNotes = EXCLUDED.personNotes, fatherName = EXCLUDED.fatherName, fatherNumber = EXCLUDED.fatherNumber, motherName = EXCLUDED.motherName, motherNum = EXCLUDED.motherNum, dateOfBirth = EXCLUDED.dateOfBirth, personAddress = EXCLUDED.personAddress, personOrgId = EXCLUDED.personOrgId, personGroupUid = EXCLUDED.personGroupUid, personMasterChangeSeqNum = EXCLUDED.personMasterChangeSeqNum, personLocalChangeSeqNum = EXCLUDED.personLocalChangeSeqNum, personLastChangedBy = EXCLUDED.personLastChangedBy, personLct = EXCLUDED.personLct, personCountry = EXCLUDED.personCountry, personType = EXCLUDED.personType
             */"""
         ]
     )
 ))
@Serializable
open class Person() {

    @PrimaryKey(autoGenerate = true)
    var personUid: Long = 0

    var username: String? = null

    var firstNames: String? = ""

    var lastName: String? = ""

    var emailAddr: String? = null

    var phoneNum: String? = null

    var gender: Int = 0

    var active: Boolean = true

    var admin: Boolean = false

    var personNotes: String? = null

    var fatherName: String? = null

    var fatherNumber: String? = null

    var motherName: String? = null

    var motherNum: String? = null

    var dateOfBirth: Long = 0

    var personAddress: String? = null

    /**
     * The ID given to the person by their organization
     */
    var personOrgId: String? = null

    //The PersonGroup that is created for this individual
    var personGroupUid: Long = 0L

    @MasterChangeSeqNum
    var personMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var personLct: Long = 0

    var personCountry: String? = null

    @ColumnInfo(defaultValue = "${TYPE_NORMAL_PERSON}")
    var personType: Int = TYPE_NORMAL_PERSON


    fun fullName():String{
        var f = ""
        var l = ""
        if(firstNames != null){
            f = firstNames as String
        }
        if(lastName != null){
            l = lastName as String
        }

        return "$f $l"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Person

        if (personUid != other.personUid) return false
        if (username != other.username) return false
        if (firstNames != other.firstNames) return false
        if (lastName != other.lastName) return false
        if (emailAddr != other.emailAddr) return false
        if (phoneNum != other.phoneNum) return false
        if (gender != other.gender) return false
        if (active != other.active) return false
        if (admin != other.admin) return false
        if (personNotes != other.personNotes) return false
        if (fatherName != other.fatherName) return false
        if (fatherNumber != other.fatherNumber) return false
        if (motherName != other.motherName) return false
        if (motherNum != other.motherNum) return false
        if (dateOfBirth != other.dateOfBirth) return false
        if (personAddress != other.personAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = personUid.hashCode()
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (firstNames?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (emailAddr?.hashCode() ?: 0)
        result = 31 * result + (phoneNum?.hashCode() ?: 0)
        result = 31 * result + gender
        result = 31 * result + active.hashCode()
        result = 31 * result + admin.hashCode()
        result = 31 * result + (personNotes?.hashCode() ?: 0)
        result = 31 * result + (fatherName?.hashCode() ?: 0)
        result = 31 * result + (fatherNumber?.hashCode() ?: 0)
        result = 31 * result + (motherName?.hashCode() ?: 0)
        result = 31 * result + (motherNum?.hashCode() ?: 0)
        result = 31 * result + dateOfBirth.hashCode()
        result = 31 * result + (personAddress?.hashCode() ?: 0)
        return result
    }

    constructor(username: String, firstNames: String, lastName: String) : this() {
        this.username = username
        this.firstNames = firstNames
        this.lastName = lastName
    }

    constructor(username: String, firstNames: String, lastName: String, active:Boolean = false,
                notes:String = "", address:String = "", phone:String = "") : this() {
        this.username = username
        this.firstNames = firstNames
        this.lastName = lastName
        this.active = active
        this.personNotes = notes
        this.personAddress = address
        this.phoneNum = phone
    }

    companion object {

        const val TABLE_ID = 9

        const val GENDER_UNSET = 0

        const val GENDER_FEMALE = 1

        const val GENDER_MALE = 2

        const val GENDER_OTHER = 4

        const val TYPE_NORMAL_PERSON = 0

        const val TYPE_SYSTEM = 1

        const val JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1 = """
            JOIN ScopedGrant
                 ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                    AND (ScopedGrant.sgPermissions &"""

        //In between is where to put the required permission

        //The class subquery is most efficient and logical when ScopedGrant has already been joined
        // (e.g. we are looking to join from ScopedGrant out to person)
        const val FROM_PERSON_TO_SCOPEDGRANT_JOIN_ON_CLAUSE = """
                ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                    AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
                 OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                    AND ScopedGrant.sgEntityUid = Person.personUid)
                 OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}       
                    AND Person.personUid IN (
                        SELECT DISTINCT clazzEnrolmentPersonUid
                          FROM ClazzEnrolment
                         WHERE clazzEnrolmentClazzUid =ScopedGrant.sgEntityUid 
                           AND ClazzEnrolment.clazzEnrolmentActive))
                 OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                    AND Person.personUid IN (
                        SELECT DISTINCT schoolMemberPersonUid
                          FROM SchoolMember
                         WHERE schoolMemberSchoolUid = ScopedGrant.sgEntityUid
                           AND schoolMemberActive))
                           )    
        """


        const val JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2 = """
                                                    ) > 0
            JOIN Person 
                 ON $FROM_PERSON_TO_SCOPEDGRANT_JOIN_ON_CLAUSE
        """



        //The class/school subquery is most efficient and logical when Person has already been joined
        // (e.g. we are looking to join from Person out to ScopedGrant)
        const val FROM_SCOPEDGRANT_TO_PERSON_JOIN_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                    AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
                 OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                    AND ScopedGrant.sgEntityUid = Person.personUid)
                 OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}       
                    AND ScopedGrant.sgEntityUid IN (
                        SELECT DISTINCT clazzEnrolmentClazzUid
                          FROM ClazzEnrolment
                         WHERE clazzEnrolmentPersonUid = Person.personUid 
                           AND ClazzEnrolment.clazzEnrolmentActive))
                 OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                    AND ScopedGrant.sgEntityUid IN (
                        SELECT DISTINCT schoolMemberSchoolUid
                          FROM SchoolMember
                         WHERE schoolMemberPersonUid = Person.personUid
                           AND schoolMemberActive))
                           )
        """


        const val JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1 = """
            JOIN ScopedGrant 
                   ON $FROM_SCOPEDGRANT_TO_PERSON_JOIN_ON_CLAUSE
                   AND (ScopedGrant.sgPermissions & 
        """


        const val JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2 = """
                                                     ) > 0
             JOIN PersonGroupMember AS PrsGrpMbr
                   ON ScopedGrant.sgGroupUid = PrsGrpMbr.groupMemberGroupUid
              JOIN UserSession
                   ON UserSession.usPersonUid = PrsGrpMbr.groupMemberPersonUid
                      AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
        """

    }


}
