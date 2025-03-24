package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo

val SessionTimeAndProgressInfo.statementSummary: StatementSummaryEntity
    get() = StatementSummaryEntity(successful = isSuccessful, completed = isCompleted)
