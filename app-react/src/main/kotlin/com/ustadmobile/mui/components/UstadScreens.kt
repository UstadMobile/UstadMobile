package com.ustadmobile.mui.components

import com.ustadmobile.MuiAppState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.entities.USTAD_SCREENS
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.view.Content
import csstype.Auto
import csstype.Display
import csstype.GridTemplateAreas
import csstype.array
import mui.system.Box
import mui.system.sx
import react.FC
import react.Props
import react.useState
import remix.run.router.LoaderFunction
import kotlin.js.Promise.Companion.resolve

/**
 * As per components/Showcases.kt on the kotlin mui showcase
 */
val UstadScreens = FC<Props> {

    val mobileMode = useMediaQuery("(max-width:960px)")

    Box {
        sx {
            display = Display.grid
            gridTemplateRows = array(
                Sizes.Header.Height,
                Auto.auto,
            )
            gridTemplateColumns = array(
                Sizes.Sidebar.Width, Auto.auto,
            )
            gridTemplateAreas = GridTemplateAreas(
                arrayOf(Area.Header, Area.Header),
                if (mobileMode)
                    arrayOf(Area.Content, Area.Content)
                else
                    arrayOf(Area.Sidebar, Area.Content),
            )
        }

        Header()
        Sidebar {
            visible = true
        }
        Content()
    }

//    val mobileMode = false//useMediaQuery("(max-width:960px)")
//    var appUiState: AppUiState by useState { AppUiState() }
//
//    var muiAppState: MuiAppState by useState { MuiAppState() }
//
//    Box {
//        sx {
//            display = Display.grid
//            gridTemplateRows = array(
//                Sizes.Header.Height,
//                Auto.auto,
//            )
//            gridTemplateColumns = array(
//                Sizes.Sidebar.Width, Auto.auto,
//            )
//
//            //As per https://developer.mozilla.org/en-US/docs/Web/CSS/grid-template-areas
//            gridTemplateAreas = GridTemplateAreas(
//                arrayOf(Area.Header, Area.Header),
//                if (mobileMode || !appUiState.navigationVisible)
//                    arrayOf(Area.Content, Area.Content)
//                else
//                    arrayOf(Area.Sidebar, Area.Content),
//            )
//        }
//
//        Header {
//            this.appUiState = appUiState
//            setAppBarHeight = {
//                if(muiAppState.appBarHeight != it)
//                    muiAppState = muiAppState.copy( appBarHeight = it)
//            }
//        }
//
//        //if (mobileMode) Menu() else Sidebar()
//        //Note: If we remove the component, instead of hiding using Display property,
//        // then this seems to make react destroy the content component and create a
//        // completely new one, which we definitely do not want
//        Sidebar {
//            visible = appUiState.navigationVisible
//        }
//
//        Content {
//            this.muiAppState = muiAppState
//            onAppUiStateChanged = {
//                appUiState = it
//            }
//        }
//    }
}

/**
 * As per components/Showcases on MUI showcase
 */

val ustadScreensLoader: LoaderFunction = {
    console.log("ustadScreenLoader: resolve")
    resolve(USTAD_SCREENS)
}

