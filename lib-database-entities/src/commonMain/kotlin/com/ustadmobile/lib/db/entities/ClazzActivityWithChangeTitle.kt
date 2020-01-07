package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzActivityWithChangeTitle : ClazzActivity() {

    var changeTitle: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzActivityWithChangeTitle

        if (changeTitle != other.changeTitle) return false
        if(clazzActivityDone != other.clazzActivityDone) return false
        if(isClazzActivityGoodFeedback != other.isClazzActivityGoodFeedback) return false
        if(clazzActivityQuantity != other.clazzActivityQuantity) return false
        if(clazzActivityClazzUid != other.clazzActivityClazzUid) return false
        if(clazzActivityClazzActivityChangeUid != other.clazzActivityClazzActivityChangeUid) return false

        return true
    }

    override fun hashCode(): Int {
        return changeTitle?.hashCode() ?: 0
    }


}
