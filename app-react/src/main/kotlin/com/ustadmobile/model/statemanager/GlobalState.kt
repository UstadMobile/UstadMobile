package com.ustadmobile.model.statemanager

import com.ccfraser.muirwik.components.styles.Theme
import com.ustadmobile.util.DummyStore
import org.kodein.di.DI
import org.w3c.dom.events.Event
import redux.RAction

/**
 * Global app state, it is a shared state which can be accessed and changed by any component
 */
data class GlobalState(var showFab: Boolean = false, var fabLabel: String = "",
                       var fabIcon: String = "", var onFabClicked:(Event) -> Unit = {},
                       var di: DI = DI.lazy {}, var theme: Theme? = null,
                       var snackBarMessage: String ? = null, var type: RAction? = null,
                       var snackBarActionLabel: String? = null, var title:String? = null,
                       var onSnackActionClicked:(Event) -> Unit = {},
                       var onTabChanged:(Any) -> Unit = {}, var tabLabels: List<String> = listOf(),
                       var selectedTab: Any? = Any(), var tabKeys: List<Any> = listOf(),
                       var dummyStore: DummyStore = DummyStore()
)
