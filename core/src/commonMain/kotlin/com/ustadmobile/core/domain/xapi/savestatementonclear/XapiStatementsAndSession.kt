package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.serialization.Serializable

@Serializable
data class XapiStatementsAndSession(
    val statements: List<XapiStatement>,
    val session: XapiSessionEntity,
)
