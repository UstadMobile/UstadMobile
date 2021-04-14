package com.ustadmobile.model.statemanager

import org.w3c.dom.events.Event
import redux.RAction

/**
 * State action which defines all attributes that can be changes on a FAB by any component
 * inside the app
 */
data class FabState(var label: String = "", var visible: Boolean = false,
                    var icon: String = "", var onClick:(Event)-> Unit = {}): RAction
