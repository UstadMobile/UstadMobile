package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.StatementEntity
import kotlin.jvm.JvmStatic

object StatementConstants {

    @JvmStatic
    val STATEMENT_RESULT_OPTIONS = mapOf(
            StatementEntity.RESULT_SUCCESS to MessageID.success,
            StatementEntity.RESULT_FAILURE to MessageID.failed,
            StatementEntity.RESULT_UNSET to MessageID.unset)

}