package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.StatementEntity
import kotlin.jvm.JvmField

object StatementConstants {

    @JvmField
    val STATEMENT_RESULT_OPTIONS = mapOf(
            StatementEntity.RESULT_SUCCESS.toInt() to MessageID.success,
            StatementEntity.RESULT_FAILURE.toInt() to MessageID.failed,
            StatementEntity.RESULT_UNSET.toInt() to MessageID.unset)

}