package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = SiteTerms.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "siteterms_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         TRIGGER_UPSERT_WHERE_NEWER
     ]
 )
))
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

    @ReplicateLastModified
    @ReplicateEtag
    var sTermsLct: Long = 0

    companion object {

        const val TABLE_ID = 272

    }

}