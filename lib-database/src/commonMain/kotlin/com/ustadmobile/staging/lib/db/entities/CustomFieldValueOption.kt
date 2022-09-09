package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
class CustomFieldValueOption {

    @PrimaryKey(autoGenerate = true)
    var customFieldValueOptionUid: Long = 0

    //name of the option
    var customFieldValueOptionName: String? = null

    //custom field uid
    var customFieldValueOptionFieldUid: Long = 0

    //icon string
    var customFieldValueOptionIcon: String? = null

    //title string (message id)
    var customFieldValueOptionMessageId: Int = 0

    //active
    var customFieldValueOptionActive: Boolean = false

    @MasterChangeSeqNum
    var customFieldValueOptionMCSN: Long = 0

    @LocalChangeSeqNum
    var customFieldValueOptionLCSN: Long = 0

    @LastChangedBy
    var customFieldValueOptionLCB: Int = 0

    @LastChangedTime
    var customFieldValueLct: Long = 0

    override fun toString(): String {
        return customFieldValueOptionName ?: "unnamed option"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CustomFieldValueOption

        if (customFieldValueOptionUid != other.customFieldValueOptionUid) return false
        if (customFieldValueOptionName != other.customFieldValueOptionName) return false
        if (customFieldValueOptionFieldUid != other.customFieldValueOptionFieldUid) return false
        if (customFieldValueOptionIcon != other.customFieldValueOptionIcon) return false
        if (customFieldValueOptionMessageId != other.customFieldValueOptionMessageId) return false
        if (customFieldValueOptionActive != other.customFieldValueOptionActive) return false
        if (customFieldValueOptionMCSN != other.customFieldValueOptionMCSN) return false
        if (customFieldValueOptionLCSN != other.customFieldValueOptionLCSN) return false
        if (customFieldValueOptionLCB != other.customFieldValueOptionLCB) return false

        return true
    }

    override fun hashCode(): Int {
        var result = customFieldValueOptionUid.hashCode()
        result = 31 * result + (customFieldValueOptionName?.hashCode() ?: 0)
        result = 31 * result + customFieldValueOptionFieldUid.hashCode()
        result = 31 * result + (customFieldValueOptionIcon?.hashCode() ?: 0)
        result = 31 * result + customFieldValueOptionMessageId
        result = 31 * result + customFieldValueOptionActive.hashCode()
        result = 31 * result + customFieldValueOptionMCSN.hashCode()
        result = 31 * result + customFieldValueOptionLCSN.hashCode()
        result = 31 * result + customFieldValueOptionLCB
        return result
    }
}
