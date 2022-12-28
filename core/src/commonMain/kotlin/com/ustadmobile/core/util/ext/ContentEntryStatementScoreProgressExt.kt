package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.CONTENT_COMPLETE
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.CONTENT_FAILED
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.CONTENT_INCOMPLETE
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.CONTENT_PASSED
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_FAILURE
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_SUCCESS
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_UNSET

fun ContentEntryStatementScoreProgress.isContentComplete(): Int {

    if(contentComplete) {

        return when (success) {
            RESULT_SUCCESS -> {
                CONTENT_PASSED
            }
            RESULT_FAILURE -> {
                CONTENT_FAILED
            }
            RESULT_UNSET -> {
                CONTENT_COMPLETE
            }
            else -> {
                CONTENT_COMPLETE
            }
        }
    }
    return CONTENT_INCOMPLETE
}

fun ContentEntryStatementScoreProgress.calculateScoreWithPenalty(): Int{
    return ((((resultScore / resultMax.toFloat()) * 100) * (totalCompletedContent / totalContent.toFloat())) * (1 - (penalty.toFloat()/100))).toInt()
}


fun ContentEntryStatementScoreProgress.calculateScoreWithWeight(): Int{
    return ((resultScaled / resultWeight) * (totalCompletedContent / totalContent.toFloat())).toInt()
}

fun ContentEntryStatementScoreProgress.progressBadge(): Int {

}