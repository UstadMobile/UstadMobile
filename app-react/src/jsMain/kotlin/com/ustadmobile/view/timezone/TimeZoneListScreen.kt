package com.ustadmobile.view.timezone

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.util.ext.formattedString
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.timezone.TimezoneListUiState
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.objects.jso
import kotlinx.datetime.TimeZone
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import react.*

external interface TimeZoneListProps: Props {

    var uiState: TimezoneListUiState

    var onListItemClick: (TimeZone) -> Unit

}

val TimeZoneListComponent = FC<TimeZoneListProps> {props ->

    val muiAppState = useMuiAppState()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            items(
                list = props.uiState.timeZoneList,
                key = { it.id }
            ) { timeZoneItem ->
                TimeZoneListItem.create {
                    timeZone = timeZoneItem
                    onClick = props.onListItemClick
                }
            }
        }


        Container {
            VirtualListOutlet()
        }
    }
}

external interface TimeZoneListItemProps: Props {

    var timeZone: TimeZone?

    var onClick: (TimeZone) -> Unit

}

val TimeZoneListItem = FC<TimeZoneListItemProps> { props ->

    val labelStr = useMemo(props.timeZone?.id) {
        props.timeZone?.formattedString() ?: ""
    }

    ListItem {
        ListItemButton {
            onClick = {
                props.timeZone?.also { props.onClick(it) }
            }

            ListItemText {
                primary = ReactNode(labelStr)
            }
        }
    }

}

val TimeZoneListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        TimeZoneListViewModel(di, savedStateHandle)
    }

    val uiStateVar: TimezoneListUiState by viewModel.uiState.collectAsState(TimezoneListUiState())

    TimeZoneListComponent {
        uiState = uiStateVar
        onListItemClick = viewModel::onClickEntry

    }
}
