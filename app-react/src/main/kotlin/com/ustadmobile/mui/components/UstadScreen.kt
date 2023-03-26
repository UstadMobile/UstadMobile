package com.ustadmobile.mui.components

import com.ustadmobile.entities.USTAD_SCREENS
import com.ustadmobile.entities.UstadScreen
import js.core.get
import react.FC
import react.Props
import react.router.useLoaderData
import remix.run.router.LoaderFunction
import kotlin.js.Promise.Companion.resolve

//As per components/Showcase.kt on mui showcase #d71c6d1

val UstadScreen = FC<Props> {
    useLoaderData().unsafeCast<UstadScreen<out Props>>().component()
}

val ustadScreenLoader: LoaderFunction = { args ->
    resolve(USTAD_SCREENS.single { it.key == args.params["ustadScreenId"] })
}
