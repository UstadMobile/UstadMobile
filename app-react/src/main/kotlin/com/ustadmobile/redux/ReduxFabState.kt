package com.ustadmobile.redux

import org.w3c.dom.events.Event
import redux.RAction

data class ReduxFabState(var icon: String? = null,
                         var title: String? = null,
                         var visible: Boolean = false,
                         var onClick: ((Event) -> Unit)? = {}): RAction
