package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import kotlin.js.JsName

@Repository
@DoorDao
expect abstract class ContentEntryParentChildJoinDao : BaseDao<ContentEntryParentChildJoin> {

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

    @Query("""
        SELECT * 
          FROM ContentEntryParentChildJoin 
         WHERE cepcjParentContentEntryUid = :parentUid
    """)
    abstract suspend fun findListOfChildsByParentUuid(parentUid: Long): List<ContentEntryParentChildJoin>

    @Query("SELECT * FROM ContentEntryParentChildJoin WHERE " + "cepcjParentContentEntryUid = :parentUid AND cepcjChildContentEntryUid = :childUid LIMIT 1")
    abstract fun findJoinByParentChildUuids(parentUid: Long, childUid: Long): ContentEntryParentChildJoin?

    @Query("SELECT ContentEntry.* FROM ContentEntry " +
            "WHERE NOT EXISTS(SELECT cepcjUid FROM ContentEntryParentChildJoin WHERE cepcjChildContentEntryUid = ContentEntry.contentEntryUid) " +
            "AND EXISTS(SELECT cepcjUid FROM ContentEntryParentChildJoin WHERE cepcjParentContentEntryUid = ContentEntry.contentEntryUid)")
    abstract suspend fun selectTopEntries(): List<ContentEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entries: List<ContentEntryParentChildJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWithReplace(parentChildJoinDao: ContentEntryParentChildJoin)

    @Query("""
        UPDATE ContentEntryParentChildJoin 
           SET cepcjParentContentEntryUid = :contentEntryUid, 
               cepcjLct = :updateTime 
               WHERE cepcjUid IN (:selectedItems)
    """)
    abstract suspend fun moveListOfEntriesToNewParent(
        contentEntryUid: Long,
        selectedItems: List<Long>,
        updateTime: Long,
    )

    @Query("""
        UPDATE ContentEntryParentChildJoin
           SET cepcjDeleted = :isDeleted,
               cepcjLct = :updateTime
         WHERE cepcjUid IN (:selectedUids) 
    """)
    abstract suspend fun setEntriesDeleted(
        selectedUids: List<Long>,
        isDeleted: Boolean,
        updateTime: Long
    )


    @Query("""
        SELECT ContentEntryParentChildJoin.*
          FROM ContentEntryParentChildJoin
         WHERE  ContentEntryParentChildJoin.cepcjUid = :uid
    """)
    abstract suspend fun findByUid(uid: Long): ContentEntryParentChildJoin?

}
