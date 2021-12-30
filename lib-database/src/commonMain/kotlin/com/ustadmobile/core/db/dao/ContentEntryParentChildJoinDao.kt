package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import kotlin.js.JsName

data class UmContentEntriesWithFileSize(var numEntries: Int = 0, var fileSize: Long = 0L)

@Repository
@Dao
abstract class ContentEntryParentChildJoinDao : BaseDao<ContentEntryParentChildJoin> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<ContentEntryParentChildJoin>)

    @Query("SELECT ContentEntryParentChildJoin.* FROM " +
            "ContentEntryParentChildJoin " +
            "LEFT JOIN ContentEntry parentEntry ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = parentEntry.contentEntryUid " +
            "LEFT JOIN ContentEntry childEntry ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = childEntry.contentEntryUid " +
            "WHERE parentEntry.publik AND childEntry.publik")
    abstract fun publicContentEntryParentChildJoins(): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin")
    abstract fun all(): List<ContentEntryParentChildJoin>


    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjChildContentEntryUid = :childEntryContentUid LIMIT 1")
    abstract fun findParentByChildUuids(childEntryContentUid: Long): ContentEntryParentChildJoin?

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjChildContentEntryUid = :childEntryContentUid")
    abstract fun findListOfParentsByChildUuid(childEntryContentUid: Long): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjParentContentEntryUid = :parentUid")
    abstract fun findListOfChildsByParentUuid(parentUid: Long): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjParentContentEntryUid = :parentUid AND cepcjChildContentEntryUid = :childUid LIMIT 1")
    abstract fun findJoinByParentChildUuids(parentUid: Long, childUid: Long): ContentEntryParentChildJoin?

    @Query("WITH RECURSIVE ContentEntryRecursive(contentEntryUid,containerSize) AS " +
            "(VALUES (:contentEntryUid,  " +
            "(SELECT Container.fileSize FROM Container WHERE Container.containerContentEntryUid = :contentEntryUid " +
            "ORDER BY Container.cntLastModified DESC LIMIT 1 )) " +
            "UNION ALL " +
            "SELECT inner_pcj.cepcjChildContentEntryUid as contentEntryUid," +
            "(SELECT Container.fileSize FROM Container WHERE Container.containerContentEntryUid = inner_pcj.cepcjChildContentEntryUid " +
            "ORDER BY Container.cntLastModified DESC LIMIT 1 ) AS containerSize FROM ContentEntryParentChildJoin as inner_pcj " +
            "JOIN ContentEntryRecursive  AS outer_pcj ON outer_pcj.contentEntryUid = inner_pcj.cepcjParentContentEntryUid) " +
            " SELECT sum(ContentEntryRecursive.containerSize) as fileSize, count(*) as numEntries FROM ContentEntryRecursive WHERE containerSize != 0")
    abstract suspend fun getParentChildContainerRecursiveAsync(contentEntryUid: Long) : UmContentEntriesWithFileSize ?

    @Query("SELECT ContentEntry.* FROM ContentEntry " +
            "WHERE NOT EXISTS(SELECT cepcjUid FROM ContentEntryParentChildJoin WHERE cepcjChildContentEntryUid = ContentEntry.contentEntryUid) " +
            "AND EXISTS(SELECT cepcjUid FROM ContentEntryParentChildJoin WHERE cepcjParentContentEntryUid = ContentEntry.contentEntryUid)")
    abstract suspend fun selectTopEntries(): List<ContentEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entries: List<ContentEntryParentChildJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWithReplace(parentChildJoinDao: ContentEntryParentChildJoin)

    @Query("""UPDATE ContentEntryParentChildJoin 
               SET cepcjParentContentEntryUid = :contentEntryUid, 
               cepcjLastChangedBy =  COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
               WHERE cepcjUid IN (:selectedItems)""")
    abstract suspend fun moveListOfEntriesToNewParent(contentEntryUid: Long, selectedItems: List<Long>)
}
