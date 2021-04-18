package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Dao
@Repository
abstract class StatementDao : BaseDao<StatementEntity> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<StatementEntity>)

    @Query("SELECT * From StatementEntity LIMIT 1")
    abstract fun getOneStatement(): DoorLiveData<StatementEntity?>

    @Query("SELECT * FROM StatementEntity WHERE statementId = :id LIMIT 1")
    abstract fun findByStatementId(id: String): StatementEntity?

    @Query("SELECT * FROM StatementEntity WHERE statementId IN (:id)")
    abstract fun findByStatementIdList(id: List<String>): List<StatementEntity>

    @RawQuery
    abstract suspend fun getResults(query: DoorQuery): List<ReportData>

    open suspend fun getResults(sqlStr: String, paramsList: Array<Any>): List<ReportData> {
        return getResults(SimpleDoorQuery(sqlStr, paramsList))
    }

    @RawQuery(observedEntities = [StatementEntity::class, Person::class, XLangMapEntry::class])
    abstract fun getListResults(query: DoorQuery): DataSource.Factory<Int, StatementEntityWithDisplayDetails>


    // This is required because of above raw query
    @Query("SELECT * FROM PERSON LIMIT 1")
    abstract fun getPerson(): Person?

    @Query("SELECT * FROM XLangMapEntry LIMIT 1")
    abstract fun getXLangMap(): XLangMapEntry?


    @Query("""UPDATE StatementEntity SET extensionProgress = :progress,
            statementLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE statementUid = :uid""")
    abstract fun updateProgress(uid: Long, progress: Int)


    @Query("""
        SELECT ResultSource.personUid, ResultSource.firstNames, ResultSource.lastName,
            COUNT(DISTINCT(ResultSource.contextRegistration)) AS attempts, 
            MIN(ResultSource.timestamp) AS startDate, 
            MAX(ResultSource.timestamp) AS endDate, 
            SUM(ResultSource.resultDuration) AS duration, 
            MAX(CASE WHEN ResultSource.contentEntryRoot 
                THEN ResultSource.resultScoreScaled * 100 
                ELSE 0 END) AS score, 
            MAX(ResultSource.extensionProgress) AS progress 
        
         FROM (SELECT Person.personUid, Person.firstNames, Person.lastName, 
            StatementEntity.contextRegistration, StatementEntity.timestamp, 
            StatementEntity.resultDuration, StatementEntity.resultScoreScaled, 
            StatementEntity.contentEntryRoot, StatementEntity.extensionProgress
        
         ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2}
             LEFT JOIN StatementEntity 
                ON StatementEntity.statementPersonUid = Person.personUid 
                    WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
                        AND PersonGroupMember.groupMemberActive  
                        AND statementContentEntryUid = :contentEntryUid
                        AND Person.firstNames || ' ' || Person.lastName LIKE :searchText 
             GROUP BY StatementEntity.statementUid) AS ResultSource 
         GROUP BY ResultSource.personUid 
         ORDER BY CASE(:sortOrder) 
                WHEN $SORT_FIRST_NAME_ASC THEN ResultSource.firstNames
                WHEN $SORT_LAST_NAME_ASC THEN ResultSource.lastName
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_DESC THEN ResultSource.firstNames
                WHEN $SORT_LAST_NAME_DESC THEN ResultSource.lastName
                ELSE ''
            END DESC,
            CASE(:sortOrder)
                WHEN $SORT_LAST_ACTIVE_ASC THEN endDate 
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_LAST_ACTIVE_DESC then endDate
                ELSE 0
            END DESC
         """)
    abstract fun findPersonsWithContentEntryAttempts(contentEntryUid: Long, accountPersonUid: Long,
                                                     searchText: String, sortOrder: Int)
            : DataSource.Factory<Int, PersonWithAttemptsSummary>

    @Query("""
        SELECT MIN(timestamp) AS startDate, 
            MAX(CASE 
                    WHEN StatementEntity.resultSuccess > 0 
                    AND StatementEntity.contentEntryRoot 
                    THEN StatementEntity.resultSuccess 
                    ELSE 0 END) AS resultSuccess, 
            SUM(CASE 
                     WHEN CAST(resultCompletion AS INTEGER) > 0 
                     AND StatementEntity.contentEntryRoot 
                     THEN 1 
                     ELSE 0 END) AS resultComplete, 
            SUM(resultDuration) AS duration, contextRegistration, 
            MAX(CASE WHEN contentEntryRoot 
                     THEN resultScoreRaw ELSE 0 END) AS resultScore, 
            MAX(CASE WHEN contentEntryRoot 
                     THEN resultScoreMax ELSE 0 END) AS resultMax,
            MAX(CASE WHEN contentEntryRoot 
                     THEN resultScoreScaled ELSE 0 END) AS resultScoreScaled  
        FROM StatementEntity 
            LEFT JOIN Person 
                ON Person.personUid = StatementEntity.statementPersonUid 
        WHERE statementContentEntryUid = :contentEntryUid   
            AND statementPersonUid = :personUid 
            AND :accountPersonUid 
                IN (${PersonDao.ENTITY_PERSONS_WITH_LEARNING_RECORD_PERMISSION}) 
        GROUP BY StatementEntity.contextRegistration 
        ORDER BY startDate DESC
         """)
    abstract fun findSessionsForPerson(contentEntryUid: Long, accountPersonUid: Long, personUid: Long)
            : DataSource.Factory<Int, PersonWithSessionsDisplay>


    @Query("""
        SELECT StatementEntity.*, VerbEntity.*, 
            verbLangMap.valueLangMap AS verbDisplay, 
            xobjectMap.valueLangMap AS objectDisplay 
        FROM StatementEntity
                LEFT JOIN Person 
                    ON StatementEntity.statementPersonUid = Person.personUid 
                LEFT JOIN VerbEntity 
                    ON VerbEntity.verbUid = StatementEntity.statementVerbUid 
                LEFT JOIN XLangMapEntry verbLangMap 
                    ON verbLangMap.verbLangMapUid = VerbEntity.verbUid
                LEFT JOIN XLangMapEntry xobjectMap 
                    ON xobjectMap.objectLangMapUid = StatementEntity.xObjectUid
         WHERE statementContentEntryUid = :contentEntryUid 
            AND statementPersonUid = :personUid 
            AND contextRegistration = :contextRegistration 
            AND :accountPersonUid 
                IN (${PersonDao.ENTITY_PERSONS_WITH_LEARNING_RECORD_PERMISSION}) 
         ORDER BY StatementEntity.timestamp DESC
         """)
    abstract fun findSessionDetailForPerson(contentEntryUid: Long, accountPersonUid: Long,
                                            personUid: Long, contextRegistration: String)
            : DataSource.Factory<Int, StatementWithSessionDetailDisplay>


    @Query("""
        SELECT COUNT(DISTINCT personUid) 
          FROM Person 
                LEFT JOIN StatementEntity 
                ON Person.personUid = StatementEntity.statementPersonUid
        WHERE Person.username IS NOT NULL 
          AND Person.active
          AND (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
    """)
    abstract fun getActiveUsersUsedContent(startDateTime: Long, endDateTime: Long): Int


    @Query("""
        SELECT COUNT(DISTINCT personUid) AS count, gender
          FROM Person 
                LEFT JOIN StatementEntity 
                ON Person.personUid = StatementEntity.statementPersonUid
        WHERE Person.username IS NOT NULL 
          AND Person.active
          AND (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
        GROUP BY Person.gender
    """)
    abstract fun getActiveUsersUsedContentByGender(startDateTime: Long, endDateTime: Long): List<ActiveUsageByGender>


    @Query("""
        SELECT COUNT(DISTINCT personUid) AS count, personCountry AS country
          FROM Person 
                LEFT JOIN StatementEntity 
                ON Person.personUid = StatementEntity.statementPersonUid
        WHERE Person.username IS NOT NULL 
          AND Person.active
          AND (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
        GROUP BY Person.personCountry
    """)
    abstract fun getActiveUsersUsedContentByCountry(startDateTime: Long, endDateTime: Long): List<ActiveUsageByCountry>


    @Query("""
        SELECT COUNT(DISTINCT personUid) AS count, personConnectivityStatus AS status
          FROM Person 
                LEFT JOIN StatementEntity 
                ON Person.personUid = StatementEntity.statementPersonUid
        WHERE Person.username IS NOT NULL 
          AND Person.active
          AND (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
        GROUP BY Person.personConnectivityStatus
    """)
    abstract fun getActiveUsersUsedContentByConnectivity(startDateTime: Long, endDateTime: Long): List<ActiveUsageByConnectivity>

    @Query("""
        SELECT SUM(resultDuration) 
          FROM StatementEntity 
         WHERE (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
    """)
    abstract fun getDurationUsageOverPastDay(startDateTime: Long, endDateTime: Long): Long

    @Query("""
        SELECT SUM(resultDuration) AS duration, gender
          FROM StatementEntity 
               LEFT JOIN Person 
               ON Person.personUid = StatementEntity.statementPersonUid
         WHERE (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
         GROUP BY Person.gender
    """)
    abstract fun getDurationUsageOverPastDayByGender(startDateTime: Long, endDateTime: Long): List<DurationByGender>

    @Query("""
        SELECT SUM(resultDuration) AS duration, personCountry AS country
          FROM StatementEntity 
               LEFT JOIN Person 
               ON Person.personUid = StatementEntity.statementPersonUid
         WHERE (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
         GROUP BY Person.personCountry
    """)
    abstract fun getDurationUsageOverPastDayByCountry(startDateTime: Long, endDateTime: Long): List<DurationByCountry>

    @Query("""
        SELECT SUM(resultDuration) AS duration, personConnectivityStatus AS status
          FROM StatementEntity 
               LEFT JOIN Person 
               ON Person.personUid = StatementEntity.statementPersonUid
         WHERE (StatementEntity.timestamp >= :startDateTime 
                AND StatementEntity.timestamp <= :endDateTime)
         GROUP BY Person.personConnectivityStatus
    """)
    abstract fun getDurationUsageOverPastDayByConnectivity(startDateTime: Long, endDateTime: Long): List<DurationByConnectivity>

    @Serializable
    data class DurationByGender(val duration: Long, val gender: Int)

    @Serializable
    data class DurationByCountry(val duration: Long, val country: String)

    @Serializable
    data class DurationByConnectivity(val duration: Long, val status: Int)

    @Serializable
    data class ActiveUsageByGender(val count: Int, val gender: Int)

    @Serializable
    data class ActiveUsageByCountry(val count: Int, val country: String)

    @Serializable
    data class ActiveUsageByConnectivity(val count: Int, val status: Int)


    @Serializable
    data class ReportData(var yAxis: Float = 0f, var xAxis: String? = "", var subgroup: String? = "")

    companion object{

        const val SORT_FIRST_NAME_ASC = 1

        const val SORT_FIRST_NAME_DESC = 2

        const val SORT_LAST_NAME_ASC = 3

        const val SORT_LAST_NAME_DESC = 4

        const val SORT_LAST_ACTIVE_ASC = 5

        const val SORT_LAST_ACTIVE_DESC = 6


    }


}
