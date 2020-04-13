package com.ustadmobile.port.android.util.ext

import androidx.navigation.NavController
import com.ustadmobile.core.util.ext.toStringMap

fun NavController.currentBackStackEntrySavedStateMap() = this.currentBackStackEntry?.savedStateHandle?.toStringMap()

