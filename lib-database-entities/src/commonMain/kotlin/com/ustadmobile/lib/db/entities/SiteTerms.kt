package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
//@SyncableEntity(tableId = SiteTerms.TABLE_ID)
@ReplicateEntity(tableId = SiteTerms.TABLE_ID, tracker = SiteTermsReplicate::class)
open class SiteTerms {

    @PrimaryKey(autoGenerate = true)
    var sTermsUid: Long = 0

    var termsHtml: String? = null

    //Two letter code for easier direct queries
    var sTermsLang: String? = null

    //Foreign key to the language object
    var sTermsLangUid: Long = 0

    var sTermsActive: Boolean = true

    @LastChangedBy
    var sTermsLastChangedBy: Int = 0

    @MasterChangeSeqNum
    var sTermsPrimaryCsn: Long = 0

    @LocalChangeSeqNum
    var sTermsLocalCsn: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var sTermsLct: Long = 0

    companion object {

        const val TABLE_ID = 272

    }

}