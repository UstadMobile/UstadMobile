package com.ustadmobile.model.statemanager

import com.ccfraser.muirwik.components.styles.Theme
import org.kodein.di.DI
import org.w3c.dom.events.Event
import redux.RAction

/**
 * Global app state, it is a shared state which can be accessed and changed by any component
 */
data class GlobalState(var title: String? = "", var showFab: Boolean = false,
                       var fabLabel: String = "", var fabIcon: String = "",
                       var onFabClicked:(Event) -> Unit = {}, var di: DI = DI.lazy {},
                       var theme: Theme? = null, var loading: Boolean = false,
                       var snackBarMessage: String ? = null, var type: RAction? = null,
                       var snackBarActionLabel: String? = null,
                       var onSnackActionClicked:(Event) -> Unit = {}
)
