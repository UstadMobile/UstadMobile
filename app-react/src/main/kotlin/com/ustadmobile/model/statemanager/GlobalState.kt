package com.ustadmobile.model.statemanager

import org.kodein.di.DI
import org.w3c.dom.events.Event

data class UmState(var title: String? = "", var showFab: Boolean = false, var fabLabel: String = "",
                   var fabIcon: String = "", var onClick:(Event) -> Unit = {},
                   var di: DI = DI.lazy {})
