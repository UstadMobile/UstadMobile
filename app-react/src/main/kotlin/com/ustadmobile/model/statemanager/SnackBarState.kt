package com.ustadmobile.model.statemanager

import org.w3c.dom.events.Event
import redux.RAction

/**
 * State action which defines all attributes that can be changes on a Snackbar by any component
 * inside the app
 */
data class SnackBarState(var message: String? = null, var actionLabel: String? = null,
                         var onClick:(Event)-> Unit = {}): RAction
