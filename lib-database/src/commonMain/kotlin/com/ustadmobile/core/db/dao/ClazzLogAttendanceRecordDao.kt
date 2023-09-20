package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class ClazzLogAttendanceRecordDao : BaseDao<ClazzLogAttendanceRecord> {

    @Insert
    abstract suspend fun insertListAsync(entities: List<ClazzLogAttendanceRecord>)

    @Query("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLogAttendanceRecord?

    @Update
    abstract suspend fun updateListAsync(entities: List<ClazzLogAttendanceRecord>)


    @Query("""SELECT ClazzLogAttendanceRecord.*, Person.*
         FROM ClazzLogAttendanceRecord 
         LEFT JOIN Person ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = Person.personUid
         WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid""")
    abstract suspend fun findByClazzLogUid(clazzLogUid: Long): List<ClazzLogAttendanceRecordWithPerson>

    @Query("""
        UPDATE ClazzLogAttendanceRecord
           SET clazzLogAttendanceRecordClazzLogUid = :newClazzLogUid,
               clazzLogAttendanceRecordLastChangedTime = :changedTime
        WHERE clazzLogAttendanceRecordClazzLogUid = :oldClazzLogUid
    """)
    abstract fun updateRescheduledClazzLogUids(
        oldClazzLogUid: Long,
        newClazzLogUid: Long,
        changedTime: Long
    )

    @Query("""
        ${ClazzEnrolmentDaoCommon.WITH_CURRENTLY_ENROLED_STUDENTS_SQL}
                  
        SELECT Person.*, ClazzLogAttendanceRecord.*
          FROM Person
               LEFT JOIN ClazzLogAttendanceRecord 
                         ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid = 
                            (SELECT ClazzLogAttendanceRecordInner.clazzLogAttendanceRecordUid  
                               FROM ClazzLogAttendanceRecord ClazzLogAttendanceRecordInner
                              WHERE ClazzLogAttendanceRecordInner.clazzLogAttendanceRecordClazzLogUid = :clazzLogUid
                                AND ClazzLogAttendanceRecordInner.clazzLogAttendanceRecordPersonUid = Person.personUid
                           ORDER BY ClazzLogAttendanceRecordInner.clazzLogAttendanceRecordLastChangedTime DESC     
                              LIMIT 1  
                            )
         WHERE Person.personUid IN 
               (SELECT CurrentlyEnrolledPersonUids.enroledPersonUid
                  FROM CurrentlyEnrolledPersonUids)                
    """)
    abstract suspend fun findByClazzAndTime(
        clazzUid: Long,
        clazzLogUid: Long,
        time: Long
    ): List<PersonAndClazzLogAttendanceRecord>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entityList: List<ClazzLogAttendanceRecord>)

}
