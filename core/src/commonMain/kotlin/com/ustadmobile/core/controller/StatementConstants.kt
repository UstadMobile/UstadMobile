package com.ustadmobile.core.controller

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.StatementEntity
import kotlin.jvm.JvmField

object StatementConstants {

    @JvmField
    val STATEMENT_RESULT_OPTIONS = mapOf(
            StatementEntity.RESULT_SUCCESS.toInt() to MR.strings.success,
            StatementEntity.RESULT_FAILURE.toInt() to MR.strings.failed,
            StatementEntity.RESULT_UNSET.toInt() to MR.strings.unset)

}