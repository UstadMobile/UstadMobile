package com.ustadmobile.core.contentformats.xapi.endpoints

import com.ustadmobile.core.domain.xapi.model.XapiStatement
import org.kodein.di.DIAware

interface XapiStatementEndpoint : DIAware {

    fun storeStatements(statements: List<XapiStatement>, statementId: String,
                        contentEntryUid: Long = 0L, clazzUid: Long = 0L): List<String>

}