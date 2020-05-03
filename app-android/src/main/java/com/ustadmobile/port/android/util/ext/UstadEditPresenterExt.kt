package com.ustadmobile.port.android.util.ext

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.setAllFromMap

fun UstadEditPresenter<*, *>.saveToStateHandle(savedStateHandle: SavedStateHandle?) {
    mutableMapOf<String, String>().also {
        this.onSaveInstanceState(it)
        savedStateHandle?.setAllFromMap(it)
    }
}

fun UstadEditPresenter<*, *>.saveStateToCurrentBackStackStateHandle(navController: NavController) {
    saveToStateHandle(navController.currentBackStackEntry?.savedStateHandle)
}
