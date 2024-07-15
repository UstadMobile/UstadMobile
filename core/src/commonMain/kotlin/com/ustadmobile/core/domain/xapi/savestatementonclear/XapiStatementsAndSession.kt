package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import kotlinx.serialization.Serializable

@Serializable
data class XapiStatementsAndSession(
    val statements: List<XapiStatement>,
    val session: XapiSession,
)
