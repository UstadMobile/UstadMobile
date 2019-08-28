package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Language.Companion.TABLE_ID


@Entity
@SyncableEntity(tableId = TABLE_ID)
class Language() {

    @PrimaryKey(autoGenerate = true)
    var langUid: Long = 0

    var name: String? = null

    // 2 letter code
    var iso_639_1_standard: String? = null

    // 3 letter code
    var iso_639_2_standard: String? = null

    // 3 letter code
    var iso_639_3_standard: String? = null

    @LocalChangeSeqNum
    var langLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var langMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var langLastChangedBy: Int = 0

    override fun toString(): String {
        return name.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val language = other as Language?

        if (langUid != language!!.langUid) return false
        if (if (name != null) name != language.name else language.name != null) return false
        if (if (iso_639_1_standard != null) iso_639_1_standard != language.iso_639_1_standard else language.iso_639_1_standard != null)
            return false
        if (if (iso_639_2_standard != null) iso_639_2_standard != language.iso_639_2_standard else language.iso_639_2_standard != null)
            return false
        return if (iso_639_3_standard != null) iso_639_3_standard == language.iso_639_3_standard else language.iso_639_3_standard == null
    }

    override fun hashCode(): Int {
        var result = (langUid xor langUid.ushr(32)).toInt()
        result = 31 * result + if (name != null) name!!.hashCode() else 0
        result = 31 * result + if (iso_639_1_standard != null) iso_639_1_standard!!.hashCode() else 0
        result = 31 * result + if (iso_639_2_standard != null) iso_639_2_standard!!.hashCode() else 0
        result = 31 * result + if (iso_639_3_standard != null) iso_639_3_standard!!.hashCode() else 0
        return result
    }

    companion object {

        const val TABLE_ID = 13
    }
}
