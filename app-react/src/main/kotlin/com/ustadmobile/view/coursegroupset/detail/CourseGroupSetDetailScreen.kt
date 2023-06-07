package com.ustadmobile.view.coursegroupset.detail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailUiState
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
import csstype.px
import js.core.jso
import mui.icons.material.AccountCircle
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface CourseGroupSetDetailProps: Props {
    var uiState: CourseGroupSetDetailUiState
}

val CourseGroupSetDetailComponent = FC<CourseGroupSetDetailProps> { props ->

    val strings = useStringsXml()

    val muiAppState = useMuiAppState()

    val groupRange = (1..(props.uiState.courseGroupSet?.cgsTotalGroups ?: 1))

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            groupRange.forEach { groupNum ->
                val groupMembers = props.uiState.membersList.filter { it.cgm?.cgmGroupNumber == groupNum }

                if(groupMembers.isNotEmpty()) {
                    item(key = "item_header_$groupNum") {
                        Typography.create {
                            sx {
                                paddingLeft = 16.px
                            }

                            + "${strings[MessageID.group]} $groupNum"
                            variant = TypographyVariant.body1
                        }
                    }

                    items(
                        list = groupMembers,
                        key = { "person_${it.personUid}" }
                    ) { member ->
                        ListItem.create {
                            ListItemIcon{
                                AccountCircle()
                            }

                            ListItemText{
                                primary = ReactNode(member.name)
                            }
                        }
                    }
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }
    }

}

val CourseGroupSetDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CourseGroupSetDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CourseGroupSetDetailUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    CourseGroupSetDetailComponent {
        uiState = uiStateVal
    }

    UstadFab {
        fabState = appState.fabState
    }
}

val CourseGroupSetDetailScreenPreview = FC<Props> {
    CourseGroupSetDetailComponent{
        uiState = CourseGroupSetDetailUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "Group 1"
                cgsTotalGroups = 4
            },
            membersList = listOf(
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson",
                    personUid = 1L
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry",
                    personUid = 2L
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry",
                    personUid = 3L
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz",
                    personUid = 4L
                )
            )
        )
    }
}