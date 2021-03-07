package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Person.Companion.ENTITY_PERSONS_WITH_PERMISSION_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.ENTITY_PERSONS_WITH_PERMISSION_PT2
import com.ustadmobile.lib.db.entities.Person.Companion.ENTITY_PERSONS_WITH_PERMISSION_PT4
import com.ustadmobile.lib.db.entities.Person.Companion.TABLE_ID
import kotlinx.serialization.Serializable

/**
 * Created by mike on 3/8/18.
 */

@Entity
@SyncableEntity(tableId = TABLE_ID, notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN Person ON ChangeLog.chTableId = $TABLE_ID AND ChangeLog.chEntityPk = Person.personUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( $ENTITY_PERSONS_WITH_PERMISSION_PT1 0 $ENTITY_PERSONS_WITH_PERMISSION_PT2 ${Role.PERMISSION_PERSON_SELECT} $ENTITY_PERSONS_WITH_PERMISSION_PT4 )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT Person.*
        FROM
         DeviceSession
         JOIN PersonGroupMember ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
         LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
         LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
         LEFT JOIN Person ON CAST((SELECT admin FROM Person Person_Admin WHERE Person_Admin.personUid = DeviceSession.dsPersonUid) AS INTEGER) = 1
             OR (Person.personUid = DeviceSession.dsPersonUid)
             OR ((Role.rolePermissions & ${Role.PERMISSION_PERSON_SELECT}) > 0 AND (EntityRole.erTableId= ${Person.TABLE_ID} AND EntityRole.erEntityUid = Person.personUid)
             OR ((Role.rolePermissions & ${Role.PERMISSION_PERSON_SELECT}) > 0 AND EntityRole.erTableId = ${Clazz.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzEnrolmentClazzUid FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = Person.personUid))
             OR ((Role.rolePermissions & ${Role.PERMISSION_PERSON_SELECT}) > 0 AND EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = Person.personUid)) OR
             ((Role.rolePermissions & ${Role.PERMISSION_PERSON_SELECT}) > 0 AND EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (
             SELECT DISTINCT Clazz.clazzSchoolUid 
             FROM Clazz
             JOIN ClazzEnrolment ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid AND ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
             )))
         WHERE
         DeviceSession.dsDeviceId = :clientId
        """
    )
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

    //This person's groupUid
    var personGroupUid: Long = 0L

    @MasterChangeSeqNum
    var personMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personLastChangedBy: Int = 0

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

        const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person_Perm.personUid FROM Person Person_Perm
            LEFT JOIN PersonGroupMember ON Person_Perm.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE
            CAST(Person_Perm.admin AS INTEGER) = 1 OR ( (
            """

        const val ENTITY_PERSONS_WITH_PERMISSION_PT2 =  """
            = 0) AND (Person_Perm.personUid = Person.personUid))
            OR
            (
            ((EntityRole.erTableId = ${Person.TABLE_ID} AND EntityRole.erEntityUid = Person.personUid) OR 
            (EntityRole.erTableId = ${Clazz.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzEnrolmentClazzUid FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = Person.personUid)) OR
            (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = Person.PersonUid)) OR
            (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (
                SELECT DISTINCT Clazz.clazzSchoolUid 
                FROM Clazz
                JOIN ClazzEnrolment ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid AND ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
            ))
            ) 
            AND (Role.rolePermissions & 
        """

        const val ENTITY_PERSONS_WITH_PERMISSION_PT4 = ") > 0)"

        const val FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1 = """
            FROM
             PersonGroupMember
             LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
             LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid 
             LEFT JOIN Person ON
             CAST((SELECT admin FROM Person Person_Admin WHERE Person_Admin.personUid = :accountPersonUid) AS INTEGER) = 1
                 OR (Person.personUid = :accountPersonUid)
             OR ((Role.rolePermissions & """

        const val FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2 = """) > 0
                 AND ((EntityRole.erTableId= ${Person.TABLE_ID} AND EntityRole.erEntityUid = Person.personUid)
                 OR (EntityRole.erTableId = ${Clazz.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzEnrolmentClazzUid FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = Person.personUid))
                 OR (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = Person.personUid)) OR
                 (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (
                 SELECT DISTINCT Clazz.clazzSchoolUid 
                 FROM Clazz
                 JOIN ClazzEnrolment ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid AND ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
                 ))))"""


    }


}
