package com.ustadmobile.util.ext

import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.navigation.UstadSavedStateHandleJs
import com.ustadmobile.core.util.ext.toStringMap


fun UstadNavController.currentBackStackEntrySavedStateMap() = (this.currentBackStackEntry?.savedStateHandle as UstadSavedStateHandleJs).toStringMap()