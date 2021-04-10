package com.ustadmobile.model.statemanager

import redux.RAction

data class UmFab(val showFab: Boolean = false, val isDetailScreen: Boolean = false): RAction
