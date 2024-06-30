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
data class Person(
    @PrimaryKey(autoGenerate = true)
    var personUid: Long = 0,

    var username: String? = null,

    var firstNames: String? = "",

    var lastName: String? = "",

    var emailAddr: String? = null,

    var phoneNum: String? = null,

    var gender: Int = 0,

    var active: Boolean = true,

    /**
     * The date of birth of the user in milliseconds since 1/Jan/1970 (UTC). All date of birth
     * timestamps are stored as TimeZone = UTC.
     */
    var dateOfBirth: Long = 0,

    var personAddress: String? = null,

    /**
     * The ID given to the person by their organization
     */
    var personOrgId: String? = null,

    //The PersonGroup that is created for this individual
    var personGroupUid: Long = 0L,

    @ReplicateLastModified
    @ReplicateEtag
    var personLct: Long = 0,

    var personCountry: String? = null,

    @ColumnInfo(defaultValue = "${TYPE_NORMAL_PERSON}")
    var personType: Int = TYPE_NORMAL_PERSON,
) {

    fun fullName() = buildString {
        firstNames?.also { append(it) }

        if(firstNames != null && lastName != null)
            append(" ")

        lastName?.also { append(it) }
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
