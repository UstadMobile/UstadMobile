package com.ustadmobile.util.ext

import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.navigation.UstadSavedStateHandleJs


fun UstadNavController.currentBackStackEntrySavedStateMap() = (this.currentBackStackEntry?.savedStateHandle as UstadSavedStateHandleJs).toStringMap()