package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * Represents an actual person in the system. May or may not have a user account.
 */
@Entity
@ReplicateEntity(
    tableId = Person.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
 @Triggers(arrayOf(
     Trigger(
         name = "person_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         //Temporary check to avoid other instances (e.g. previous versions on same url) interfering.
         conditionSql = """
             SELECT 
                    ((NEW.username IS NULL
                     OR (SELECT NOT EXISTS(
                            SELECT Person.personUid
                              FROM Person
                             WHERE Person.username = NEW.username))  
                     OR NEW.personUid = 
                        (SELECT Person.personUid
                           FROM Person
                          WHERE Person.username = NEW.username)))
                  AND CAST(NEW.personLct AS BIGINT) > 
                         (SELECT COALESCE(
                                  (SELECT Person.personLct
                                     FROM Person
                                    WHERE Person.personUid = CAST(NEW.personUid AS BIGINT)), 0))   
                            
         """,
         sqlStatements = [ TRIGGER_UPSERT ]
     )
 ))
@Serializable
class Person() {

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

    /**
     * The date of birth of the user in milliseconds since 1/Jan/1970 (UTC). All date of birth
     * timestamps are stored as TimeZone = UTC.
     */
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

    @ReplicateLastModified
    @ReplicateEtag
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

        const val TYPE_GUEST = 2

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


    }


}
