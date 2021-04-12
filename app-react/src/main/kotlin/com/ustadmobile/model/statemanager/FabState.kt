package com.ustadmobile.model.statemanager

import org.w3c.dom.events.Event
import redux.RAction

/**
 * State action which defines all attributes that can be changes on a FAB by any component
 * inside the app
 */
data class FabState(val label: String = "", val visible: Boolean = false,
                    val icon: String = "", val onClick:(Event)-> Unit = {}): RAction
