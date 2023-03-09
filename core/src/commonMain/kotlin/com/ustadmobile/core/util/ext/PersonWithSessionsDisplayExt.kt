package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import com.ustadmobile.lib.db.entities.StatementEntity

fun PersonWithSessionsDisplay.contentCompleteStatus(): Int {
    if(this.resultComplete){
        return when(this.resultSuccess){
            StatementEntity.RESULT_SUCCESS -> {
                PersonWithSessionsDisplay.RESULT_SUCCESS
            }
            StatementEntity.RESULT_FAILURE -> {
                PersonWithSessionsDisplay.RESULT_FAILURE
            }
            StatementEntity.RESULT_UNSET ->{
                PersonWithSessionsDisplay.RESULT_UNSET
            }else ->{
                PersonWithSessionsDisplay.RESULT_FAILURE
            }
        }
    }else{
        return PersonWithSessionsDisplay.RESULT_INCOMPLETE
    }
}