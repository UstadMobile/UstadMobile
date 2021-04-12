package com.ustadmobile.model.statemanager

import redux.RAction

data class AppBarState (var title: String? = null, var loading: Boolean? = null): RAction