package com.ustadmobile.util.ext

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle

fun UstadEditPresenter<*, *>.saveToStateHandle(savedStateHandle: UstadSavedStateHandle?) {
    mutableMapOf<String, String>().also {
        this.onSaveInstanceState(it)
        savedStateHandle?.setAllFromMap(it)
    }
}

fun UstadEditPresenter<*, *>.saveStateToCurrentBackStackStateHandle(navController: UstadNavController) {
    saveToStateHandle(navController.currentBackStackEntry?.savedStateHandle)
}
