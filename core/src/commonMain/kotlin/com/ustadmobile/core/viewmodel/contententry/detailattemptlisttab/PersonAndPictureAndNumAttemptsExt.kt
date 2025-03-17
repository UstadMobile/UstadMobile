package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR

val PersonAndPictureAndNumAttempts.descriptionStringRes: StringResource
    get() {
        return when {
            isSuccessful == true -> MR.strings.passed
            isSuccessful == false -> MR.strings.failed
            isCompleted -> MR.strings.completed
            else -> MR.strings.incomplete
        }
    }
