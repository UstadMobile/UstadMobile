package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.LanguageVariant.Companion.TABLE_ID
import kotlinx.serialization.Serializable


@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "languagevariant_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         TRIGGER_UPSERT_WHERE_NEWER
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

    @ReplicateLastModified
    @ReplicateEtag
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
