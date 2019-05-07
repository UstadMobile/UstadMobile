package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

import com.ustadmobile.lib.db.entities.LanguageVariant.Companion.TABLE_ID


@UmEntity(tableId = TABLE_ID)
class LanguageVariant {

    @UmPrimaryKey(autoGenerateSyncable = true)
    var langVariantUid: Long = 0

    var langUid: Long = 0

    var countryCode: String? = null

    var name: String? = null

    @UmSyncLocalChangeSeqNum
    var langVariantLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var langVariantMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var langVariantLastChangedBy: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

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
