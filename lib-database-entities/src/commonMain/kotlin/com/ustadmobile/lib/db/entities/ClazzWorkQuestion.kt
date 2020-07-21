package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 202)
@Entity
@Serializable
open class ClazzWorkQuestion {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkQuestionUid: Long = 0

    var clazzWorkQuestionText: String? = null

    var clazzWorkQuestionClazzWorkUid: Long = 0

    var clazzWorkQuestionIndex: Int = 0

    var clazzWorkQuestionType: Int = 0

    var clazzWorkQuestionActive: Boolean = false

    @MasterChangeSeqNum
    var clazzWorkQuestionMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkQuestionLCSN: Long = 0

    @LastChangedBy
    var clazzWorkQuestionLCB: Int = 0

    companion object{
        const val CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT = 1
        const val CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE = 2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzWorkQuestion

        if (clazzWorkQuestionUid != other.clazzWorkQuestionUid) return false
        if (clazzWorkQuestionText != other.clazzWorkQuestionText) return false
        if (clazzWorkQuestionClazzWorkUid != other.clazzWorkQuestionClazzWorkUid) return false
        if (clazzWorkQuestionIndex != other.clazzWorkQuestionIndex) return false
        if (clazzWorkQuestionType != other.clazzWorkQuestionType) return false
        if (clazzWorkQuestionActive != other.clazzWorkQuestionActive) return false
        if (clazzWorkQuestionMCSN != other.clazzWorkQuestionMCSN) return false
        if (clazzWorkQuestionLCSN != other.clazzWorkQuestionLCSN) return false
        if (clazzWorkQuestionLCB != other.clazzWorkQuestionLCB) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzWorkQuestionUid.hashCode()
        result = 31 * result + (clazzWorkQuestionText?.hashCode() ?: 0)
        result = 31 * result + clazzWorkQuestionClazzWorkUid.hashCode()
        result = 31 * result + clazzWorkQuestionIndex
        result = 31 * result + clazzWorkQuestionType
        result = 31 * result + clazzWorkQuestionActive.hashCode()
        result = 31 * result + clazzWorkQuestionMCSN.hashCode()
        result = 31 * result + clazzWorkQuestionLCSN.hashCode()
        result = 31 * result + clazzWorkQuestionLCB
        return result
    }


}
