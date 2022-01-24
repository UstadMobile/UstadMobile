package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.LanguageVariant.Companion.TABLE_ID
import kotlinx.serialization.Serializable


@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID, tracker = LanguageVariantReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "languagevariant_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO LanguageVariant(langVariantUid, langUid, countryCode, name, langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum, langVariantLastChangedBy, langVariantLct) 
         VALUES (NEW.langVariantUid, NEW.langUid, NEW.countryCode, NEW.name, NEW.langVariantLocalChangeSeqNum, NEW.langVariantMasterChangeSeqNum, NEW.langVariantLastChangedBy, NEW.langVariantLct) 
         /*psql ON CONFLICT (langVariantUid) DO UPDATE 
         SET langUid = EXCLUDED.langUid, countryCode = EXCLUDED.countryCode, name = EXCLUDED.name, langVariantLocalChangeSeqNum = EXCLUDED.langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum = EXCLUDED.langVariantMasterChangeSeqNum, langVariantLastChangedBy = EXCLUDED.langVariantLastChangedBy, langVariantLct = EXCLUDED.langVariantLct
         */"""
     ]
 )
))
class LanguageVariant() {


    @PrimaryKey(autoGenerate = true)
    var langVariantUid: Long = 0

    var langUid: Long = 0

    var countryCode: String? = null

    var name: String? = null

    @LocalChangeSeqNum
    var langVariantLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var langVariantMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var langVariantLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var langVariantLct: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val that = other as LanguageVariant?

        if (langVariantUid != that!!.langVariantUid) return false
        if (langUid != that.langUid) return false
        if (if (countryCode != null) countryCode != that.countryCode else that.countryCode != null)
            return false
        return if (name != null) name == that.name else that.name == null
    }

    override fun hashCode(): Int {
        var result = (langVariantUid xor langVariantUid.ushr(32)).toInt()
        result = 31 * result + (langUid xor langUid.ushr(32)).toInt()
        result = 31 * result + if (countryCode != null) countryCode!!.hashCode() else 0
        result = 31 * result + if (name != null) name!!.hashCode() else 0
        return result
    }

    companion object {

        const val TABLE_ID = 10
    }
}
