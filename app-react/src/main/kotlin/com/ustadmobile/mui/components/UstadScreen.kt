package com.ustadmobile.mui.components

import com.ustadmobile.entities.USTAD_SCREENS
import com.ustadmobile.entities.UstadScreen
import react.FC
import react.Props
import react.router.useLoaderData
import remix.run.router.LoaderFunction
import kotlin.js.Promise.Companion.resolve

val UstadScreen = FC<Props> {
    useLoaderData().unsafeCast<UstadScreen>().component()
}

val ustadScreenLoader: LoaderFunction = { args ->
    resolve(USTAD_SCREENS.single { it.key == args.params["ustadScreenName"] })
}
