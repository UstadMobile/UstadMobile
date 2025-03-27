package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts

val PersonAndPictureAndNumAttempts.statementSummary: StatementSummaryEntity
    get() = StatementSummaryEntity(successful = isSuccessful, completed = isCompleted)
