package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class ContainerImportJob {

    @PrimaryKey(autoGenerate = true)
    var cijUid: Long = 0

    var cijContainerUid: Long = 0

    var cijUri: String? = null

    var cijImportMode: Int = 0

    var cijContainerBaseDir: String? = null

    var cijContentEntryUid: Long = 0

    var cijMimeType: String? = null

    var cijSessionId: String? = null

    var cijJobStatus: Int = 0

    var cijBytesSoFar: Long = 0

    var cijImportCompleted: Boolean = false

    var cijContentLength: Long = 0

    var cijContainerEntryFileUids: String? = null

    var cijConversionParams: String? = null

    companion object {

        const val SERVER_IMPORT_MODE = 1001

        const val CLIENT_IMPORT_MODE = 1002

    }

}