package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.CourseGroupSet

fun CourseGroupSet?.fallbackIndividualSet(systemImpl: UstadMobileSystemImpl, context: Any): CourseGroupSet{
    return this ?: CourseGroupSet().apply {
        cgsName = systemImpl.getString(MessageID.individual_submission, context)
    }
}