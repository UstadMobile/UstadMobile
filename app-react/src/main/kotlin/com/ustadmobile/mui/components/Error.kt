package com.ustadmobile.mui.components

import io.ktor.client.engine.js.*
import mui.material.Typography
import react.FC
import react.Props
import react.router.useRouteError

/**
 * As per components/Error on Kotlin MUI showcase
 */
val Error = FC<Props> {
    val error = useRouteError().unsafeCast<JsError>()

    Typography {
        +error.message
    }
}
