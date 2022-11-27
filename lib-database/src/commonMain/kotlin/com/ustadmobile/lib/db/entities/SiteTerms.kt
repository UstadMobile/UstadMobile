package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = SiteTerms.TABLE_ID, tracker = SiteTermsReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "siteterms_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO SiteTerms(sTermsUid, termsHtml, sTermsLang, sTermsLangUid, sTermsActive, sTermsLastChangedBy, sTermsPrimaryCsn, sTermsLocalCsn, sTermsLct) 
         VALUES (NEW.sTermsUid, NEW.termsHtml, NEW.sTermsLang, NEW.sTermsLangUid, NEW.sTermsActive, NEW.sTermsLastChangedBy, NEW.sTermsPrimaryCsn, NEW.sTermsLocalCsn, NEW.sTermsLct) 
         /*psql ON CONFLICT (sTermsUid) DO UPDATE 
         SET termsHtml = EXCLUDED.termsHtml, sTermsLang = EXCLUDED.sTermsLang, sTermsLangUid = EXCLUDED.sTermsLangUid, sTermsActive = EXCLUDED.sTermsActive, sTermsLastChangedBy = EXCLUDED.sTermsLastChangedBy, sTermsPrimaryCsn = EXCLUDED.sTermsPrimaryCsn, sTermsLocalCsn = EXCLUDED.sTermsLocalCsn, sTermsLct = EXCLUDED.sTermsLct
         */"""
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

    @LastChangedTime
    @ReplicationVersionId
    var sTermsLct: Long = 0

    companion object {

        const val TABLE_ID = 272

    }

}