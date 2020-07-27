package com.ustadmobile.core.contentformats.xapi.endpoints

import com.ustadmobile.core.contentformats.xapi.Statement

interface XapiStatementEndpoint {

    suspend fun storeStatements(statements: List<Statement>, statementId: String,
                                contentEntryUid: Long): List<String>

}