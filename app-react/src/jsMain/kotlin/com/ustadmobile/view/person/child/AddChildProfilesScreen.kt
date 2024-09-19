package com.ustadmobile.view.person.child

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.person.child.AddChildProfilesUiState
import com.ustadmobile.core.viewmodel.person.child.AddChildProfilesViewModel
import mui.material.*
import react.*
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import mui.icons.material.Add
import mui.system.sx
import react.dom.html.ReactHTML
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px


external interface AddChildProfilesProps : Props {
    var uiState: AddChildProfilesUiState
    var onClickEditChild: (Person) -> Unit
    var onClickDeleteChileProfile: (Person) -> Unit
    var onClickAddChild: () -> Unit

}

val AddChildProfilesScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        AddChildProfilesViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(AddChildProfilesUiState())
    println("childProfiles = ${uiState.childProfiles}")

    Dialog {
        open = uiState.showProfileSelectionDialog
        onClose = { _, _ ->
            viewModel.onDismissLangDialog()
        }
        Typography {
            sx {
                margin = 16.px
            }
            +useStringProvider()[MR.strings.which_profile_do_you_want_to_start]
        }
        List {
            uiState.personAndChildrenList.forEach { profile ->
                ListItem {
                    ListItemButton {
                        onClick = {
                            viewModel.onProfileSelected(profile)
                        }

                        ListItemText {
                            primary = ReactNode(profile.fullName())
                        }
                    }
                }
            }
        }
    }
    AddChildProfilesComponent2 {
        this.uiState = uiState
        onClickEditChild = viewModel::onClickEditChileProfile
        onClickDeleteChileProfile = viewModel::onClickDeleteChildProfile
        onClickAddChild = viewModel::onClickAddChileProfile
    }
}
val AddChildProfilesComponent2 = FC<AddChildProfilesProps> { props ->
    val strings = useStringProvider()
    val muiAppState = useMuiAppState()


    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {


            ListItem {
                key = "0"
                ListItemButton {
                    id = "child_profile"
                    onClick = {
                        props.onClickAddChild()
                    }

                    ListItemIcon {
                        Add()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.child_profile])

                    }
                }

            }
            props.uiState.childProfiles.forEach { person ->

                ChildProfileItem {
                    childProfile = person
                    onClickEditChild = props.onClickEditChild
                    onClickDeleteChildProfile = props.onClickDeleteChileProfile

                }
            }


        }
        Container {
            VirtualListOutlet()
        }

    }
}






