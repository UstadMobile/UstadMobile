package com.ustadmobile.model.statemanager

import org.kodein.di.DI
import org.w3c.dom.events.Event

/**
 * Global app state, it is a shared state which can be accessed and changed by any component
 */
data class GlobalState(var title: String? = "",var view:String? = null,
                       var showFab: Boolean = false, var fabLabel: String = "",
                       var fabIcon: String = "", var onClick:(Event) -> Unit = {},
                       var di: DI = DI.lazy {})
