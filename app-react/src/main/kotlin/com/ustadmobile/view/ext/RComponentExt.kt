package com.ustadmobile.view.ext

import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_ID
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_KEY
import com.ustadmobile.util.RouteManager.getArgs
import kotlinx.browser.localStorage
import kotlinx.browser.window
import react.RComponent
import react.RProps
import react.RState

fun RComponent<RProps, RState>.saveResultToBackStackSavedStateHandle(result: List<*>){
    saveResultToBackStackSavedStateHandle(JSON.stringify(result))
}

fun RComponent<RProps, RState>.saveResultToBackStackSavedStateHandle(result: String) {
    val saveToDestination = getArgs()[ARG_RESULT_DEST_ID]
    val saveToKey = getArgs()[ARG_RESULT_DEST_KEY]
    if(saveToDestination != null && saveToKey != null) {
        localStorage.setItem(saveToDestination, JSON.stringify(mapOf<String, Any>(saveToKey to result)))
        window.history.back()
    }else{
        window.history.back()
    }
}