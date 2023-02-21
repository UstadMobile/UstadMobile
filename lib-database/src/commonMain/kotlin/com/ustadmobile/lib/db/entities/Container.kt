package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity(indices = arrayOf(Index(name = "cnt_uid_to_most_recent",
        value = ["containerContentEntryUid", "cntLastModified"])))
@ReplicateEntity(tableId = Container.TABLE_ID, tracker = ContainerReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "container_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO Container(containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, cntLct, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries)
         SELECT NEW.containerUid, NEW.cntLocalCsn, NEW.cntMasterCsn, NEW.cntLastModBy, NEW.cntLct, NEW.fileSize, NEW.containerContentEntryUid, NEW.cntLastModified, NEW.mimeType, NEW.remarks, NEW.mobileOptimized, NEW.cntNumEntries 
          WHERE NEW.cntLct > 
                (SELECT COALESCE(
                        (SELECT ContainerInt.cntLct
                           FROM Container ContainerInt
                          WHERE ContainerInt.containerUid = NEW.containerUid), 0))
         /*psql ON CONFLICT (containerUid) DO UPDATE 
         SET cntLocalCsn = EXCLUDED.cntLocalCsn, cntMasterCsn = EXCLUDED.cntMasterCsn, cntLastModBy = EXCLUDED.cntLastModBy, cntLct = EXCLUDED.cntLct, fileSize = EXCLUDED.fileSize, containerContentEntryUid = EXCLUDED.containerContentEntryUid, cntLastModified = EXCLUDED.cntLastModified, mimeType = EXCLUDED.mimeType, remarks = EXCLUDED.remarks, mobileOptimized = EXCLUDED.mobileOptimized, cntNumEntries = EXCLUDED.cntNumEntries
         */"""
     ]
 )
))
@Serializable
open class Container() {

    @PrimaryKey(autoGenerate = true)
    var containerUid: Long = 0

    @LocalChangeSeqNum
    var cntLocalCsn: Long = 0

    @MasterChangeSeqNum
    var cntMasterCsn: Long = 0

    @LastChangedBy
    var cntLastModBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var cntLct: Long = 0

    var fileSize: Long = 0

    var containerContentEntryUid: Long = 0

    @ColumnInfo(index = true)
    var cntLastModified: Long = 0

    var mimeType: String? = null

    var remarks: String? = null

    var mobileOptimized: Boolean = false

    /**
     * Total number of entries in this container
     */
    var cntNumEntries: Int = 0

    constructor(contentEntry: ContentEntry) : this() {
        this.containerContentEntryUid = contentEntry.contentEntryUid
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Container) return false

        if (containerUid != other.containerUid) return false
        if (cntLocalCsn != other.cntLocalCsn) return false
        if (cntMasterCsn != other.cntMasterCsn) return false
        if (cntLastModBy != other.cntLastModBy) return false
        if (fileSize != other.fileSize) return false
        if (containerContentEntryUid != other.containerContentEntryUid) return false
        if (cntLastModified != other.cntLastModified) return false
        if (mimeType != other.mimeType) return false
        if (remarks != other.remarks) return false
        if (mobileOptimized != other.mobileOptimized) return false
        if (cntNumEntries != other.cntNumEntries) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerUid.hashCode()
        result = 31 * result + cntLocalCsn.hashCode()
        result = 31 * result + cntMasterCsn.hashCode()
        result = 31 * result + cntLastModBy
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + containerContentEntryUid.hashCode()
        result = 31 * result + cntLastModified.hashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (remarks?.hashCode() ?: 0)
        result = 31 * result + mobileOptimized.hashCode()
        result = 31 * result + cntNumEntries
        return result
    }


    companion object {

        const val TABLE_ID = 51

    }

}
