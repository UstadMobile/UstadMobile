package com.ustadmobile.core.domain.xapi.ext

import com.ustadmobile.core.domain.xapi.model.XAPI_RESULT_EXTENSION_PROGRESS
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration

val XapiStatement.resultProgressExtension: Int?
    get() = result?.extensions?.get(XAPI_RESULT_EXTENSION_PROGRESS)?.jsonPrimitive?.intOrNull

val XapiStatement.resultDurationMillis: Long?
    get() = result?.duration?.let { Duration.parseIsoString(it) }?.inWholeMilliseconds
