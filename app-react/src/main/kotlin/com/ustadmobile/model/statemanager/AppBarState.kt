package com.ustadmobile.model.statemanager

import redux.RAction

/**
 * Represents AppBar attributes that can be changes by any component within the app
 */
data class AppBarState (var title: String? = null, var loading: Boolean? = null): RAction