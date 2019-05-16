package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.lib.database.annotation.UmRestAccessible
import com.ustadmobile.lib.database.annotation.UmRestAuthorizedUidParam
import com.ustadmobile.lib.database.annotation.UmSyncCheckIncomingCanInsert
import com.ustadmobile.lib.database.annotation.UmSyncCheckIncomingCanUpdate
import com.ustadmobile.lib.database.annotation.UmSyncCountLocalPendingChanges
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges
import com.ustadmobile.lib.database.annotation.UmSyncIncoming
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing
import com.ustadmobile.lib.db.sync.SyncResponse
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity

/**
 * A base interface for DAOs which support synchronization.
 *
 * @param <T> The Entity Type
 * @param <D> The DAO Type (generally the DAO class that is implementing SyncableDao)
</D></T> */
interface SyncableDao<T, D> : BaseDao<T> {

    /**
     * Sync with the other DAO. THe other DAO should be the same class
     *
     * @param otherDao
     * @param accountPersonUid
     * @param sendLimit the maximum number of changes to upload to the server at a time
     * @param receiveLimit the maximum number of changes to receive from the server at a time
     */
    @UmSyncOutgoing
    fun syncWith(otherDao: D, accountPersonUid: Long, sendLimit: Int, receiveLimit: Int)

    /**
     * Handle an incoming sync request
     * @param incomingChanges Incoming changes that are to be received
     * @param fromLocalChangeSeqNum send changes from localChangeSequenceNumber
     * @param fromMasterChangeSeqNum send changes from masterChangeSequenceNumber
     * @param userId authorized account uid that will be running the sync
     * @param deviceId the deviceBits of the device sending the incomingChanges (to avoid those
     * changes being sent back)
     * @param limit maximum number of changes to send back
     * @return SyncResponse representing the current state of sync after this request has been processed
     */
    @UmSyncIncoming
    @UmRestAccessible
    fun handleIncomingSync(incomingChanges: List<T>, fromLocalChangeSeqNum: Long,
                           fromMasterChangeSeqNum: Long, @UmRestAuthorizedUidParam userId: Long,
                           deviceId: Int, limit: Int): SyncResponse<T>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun replaceList(entities: List<T>)

    /**
     * Find all local changes using the local change sequence number
     *
     * @param fromLocalChangeSeqNum The number to find all local changes from (inclusive)
     * @param accountPersonUid The account performing the sync
     * @return List of entities that have been locally modified since fromLocalChangeSeqNum
     */
    @UmSyncFindLocalChanges
    fun findLocalChanges(fromLocalChangeSeqNum: Long, toLocalChangeSeqNum: Long,
                         accountPersonUid: Long, localDeviceId: Int, limit: Int): List<T>

    /**
     * Find all changes made
     *
     * @param fromLocalChangeSeqNum  (inclusive)
     * @param toLocalChangeSeqNum (inclusive)
     * @param fromMasterChangeSeqNum (inclusive)
     * @param toMasterChangeSeqNum (inclusive)
     * @param accountPersonUid id of the user account that is running the sync
     * @param limit maximum number of entries to retrieve
     * @return
     */
    @UmSyncFindAllChanges
    fun syncFindAllChanges(fromLocalChangeSeqNum: Long, toLocalChangeSeqNum: Long,
                           fromMasterChangeSeqNum: Long, toMasterChangeSeqNum: Long,
                           @UmRestAuthorizedUidParam accountPersonUid: Long,
                           deviceId: Int, limit: Int): List<T>

    @UmSyncCheckIncomingCanUpdate
    fun syncFindExistingEntities(primaryKeys: List<Long>, accountPersonUid: Long): List<UmSyncExistingEntity>

    @UmSyncCheckIncomingCanInsert
    fun syncAccountCanInsert(accountPersonUid: Long): Boolean

    @UmSyncCountLocalPendingChanges
    fun countPendingLocalChanges(accountPersonUid: Long, localDeviceId: Int): Int


}
