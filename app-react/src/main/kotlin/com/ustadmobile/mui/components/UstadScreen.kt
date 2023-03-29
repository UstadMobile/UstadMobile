package com.ustadmobile.mui.components

import com.ustadmobile.entities.USTAD_SCREENS
import com.ustadmobile.entities.UstadScreen
import react.FC
import react.Props
import react.router.useLoaderData
import remix.run.router.LoaderFunction
import kotlin.js.Promise.Companion.resolve
import js.core.get

val UstadScreen = FC<Props> {
    console.info("UstadScreen: render")
    val screen = useLoaderData().unsafeCast<UstadScreen>()
    console.info("UstadScreen = $screen")

    useLoaderData().unsafeCast<UstadScreen>().component()
}

val ustadScreenLoader: LoaderFunction = { args ->
    console.info("ustadScreenLoader: load ${args.params["ustadScreenName"]}")
    resolve(USTAD_SCREENS.single { it.key == args.params["ustadScreenName"] })
}
