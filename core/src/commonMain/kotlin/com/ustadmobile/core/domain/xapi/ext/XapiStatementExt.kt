package com.ustadmobile.core.domain.xapi.ext

import com.ustadmobile.core.domain.xapi.model.XAPI_PROGRESSED_EXTENSIONS
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration

val XapiStatement.resultProgressExtension: Int?
    get() = result?.extensions?.let { extensions ->
        XAPI_PROGRESSED_EXTENSIONS.firstNotNullOfOrNull { extensionKey ->
            extensions.get(extensionKey)?.jsonPrimitive?.intOrNull
        }
    }


val XapiStatement.resultDurationMillis: Long?
    get() = result?.duration?.let { Duration.parseIsoString(it) }?.inWholeMilliseconds
