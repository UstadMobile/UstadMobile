package com.ustadmobile.model.statemanager

import org.w3c.dom.events.Event
import redux.RAction

data class UmFab(val label: String = "", val visible: Boolean = false,
                 val icon: String = "", val onClick:(Event)-> Unit = {}): RAction
