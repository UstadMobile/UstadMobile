package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR

val StatementSummaryEntity.descriptionStringRes: StringResource
    get() {
        return when {
            successful == true -> MR.strings.passed
            successful == false -> MR.strings.failed
            completed -> MR.strings.completed
            else -> MR.strings.incomplete
        }
    }
