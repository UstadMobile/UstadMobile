package com.ustadmobile.util.ext

import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.navigation.UstadSavedStateHandleJs


fun UstadNavController.currentBackStackEntrySavedStateMap() = (this.currentBackStackEntry?.savedStateHandle as UstadSavedStateHandleJs).toStringMap()