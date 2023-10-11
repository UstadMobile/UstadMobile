package com.ustadmobile.view.accountlist

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.accountlist.AccountListUiState
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.mui.components.UstadAddListItem
import web.cssom.px
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.Delete as DeleteIcon
import mui.icons.material.AccountCircle as AccountCircleIcon
import mui.icons.material.Person2 as Person2Icon
import mui.icons.material.LinkOutlined as LinkOutlinedIcon
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.aria.ariaLabel

external interface AccountListProps: Props {
    var uiState: AccountListUiState
    var onAccountListItemClick: (UserSessionWithPersonAndEndpoint) -> Unit
    var onDeleteListItemClick: (UserSessionWithPersonAndEndpoint) -> Unit
    var onAboutClick: () -> Unit
    var onAddItem: () -> Unit
    var onMyProfileClick: () -> Unit
    var onLogoutClick: () -> Unit
}

external interface AccountListItemContentProps: Props{
    var account: UserSessionWithPersonAndEndpoint
    var onDeleteListItemClick: ((UserSessionWithPersonAndEndpoint?) -> Unit)?
    var showAccountEndpoint: Boolean
}

external interface AccountListItemProps: Props {
    var onListItemClick: ((UserSessionWithPersonAndEndpoint) -> Unit)?
    var account: UserSessionWithPersonAndEndpoint
    var onDeleteListItemClick: ((UserSessionWithPersonAndEndpoint?) -> Unit)?
    var showAccountEndpoint: Boolean
}

private val AccountListItemContent = FC<AccountListItemContentProps> { props ->

    ListItemIcon{
        Icon{
            sx{
                width = 40.px
                height = 40.px
            }
            AccountCircleIcon {
                sx{
                    width = 40.px
                    height = 40.px
                }
            }
        }
    }

    Stack{
        direction = responsive(StackDirection.column)

        ListItemText{
            primary = ReactNode("${props.account.person.firstNames} ${props.account.person.lastName}")
        }

        Stack{
            direction = responsive(StackDirection.row)

            Icon{
                sx{
                    width = 20.px
                    height = 20.px
                }
                Person2Icon {
                    sx{
                        width = 20.px
                        height = 20.px
                    }
                }
            }

            ListItemText {
                sx{
                    paddingRight = 20.px
                    paddingLeft = 5.px
                }
                secondary = ReactNode(props.account.person.username ?: "")
            }

            if(props.showAccountEndpoint) {
                Icon{
                    sx{
                        width = 20.px
                        height = 20.px
                    }

                    LinkOutlinedIcon {
                        sx{
                            width = 20.px
                            height = 20.px
                        }
                    }
                }

                ListItemText{
                    sx{
                        paddingLeft = 5.px
                    }
                    secondary = ReactNode(props.account.endpoint.url)
                }
            }
        }
    }
}

val AccountListItem = FC<AccountListItemProps> { props ->
    val onListItemClick = props.onListItemClick
    val strings = useStringProvider()

    if(onListItemClick != null){
        ListItem{
            disablePadding = true

            ListItemButton {
                onClick = {
                    onListItemClick(props.account)
                }

                AccountListItemContent{
                    account = props.account
                    onDeleteListItemClick = props.onDeleteListItemClick
                    showAccountEndpoint = props.showAccountEndpoint
                }
            }

            //Note: the delete list item click listener is used ONLY on clickable accounts (e.g.
            // not on the header)
            if (props.onDeleteListItemClick != null){
                ListItemSecondaryAction{
                    Tooltip {
                        title = ReactNode(strings[MR.strings.remove])
                        IconButton {
                            ariaLabel = strings[MR.strings.remove]
                            onClick = {
                                props.onDeleteListItemClick?.invoke(props.account)
                            }
                            DeleteIcon()
                        }
                    }
                }
            }
        }
    }else{
        ListItem{
            AccountListItemContent{
                account = props.account
                showAccountEndpoint = props.showAccountEndpoint
            }
        }
    }
}

val AccountListComponent2 = FC<AccountListProps> { props ->

    val strings = useStringProvider()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            props.uiState.headerAccount?.also { headerAccount ->
                AccountListItem{
                    account = headerAccount
                }

                Stack{
                    direction = responsive(StackDirection.row)
                    spacing = responsive(10.px)

                    sx{
                        paddingLeft = 72.px
                    }

                    Button {
                        onClick = { props.onMyProfileClick() }
                        variant = ButtonVariant.outlined
                        disabled = !props.uiState.activeAccountButtonsEnabled
                        + strings[MR.strings.my_profile].uppercase()
                    }

                    Button {
                        onClick = { props.onLogoutClick() }
                        variant = ButtonVariant.outlined
                        disabled = !props.uiState.activeAccountButtonsEnabled
                        + strings[MR.strings.logout].uppercase()
                    }
                }

                Divider {
                    sx{
                        paddingTop = 10.px
                    }
                }
            }

            List {
                props.uiState.accountsList.forEach { thisAccount ->
                    AccountListItem{
                        onListItemClick = {
                            props.onAccountListItemClick(thisAccount)
                        }
                        account = thisAccount
                        onDeleteListItemClick = {
                            props.onDeleteListItemClick(thisAccount)
                        }
                    }
                }

                UstadAddListItem {
                    text = strings[MR.strings.add_another_account]
                    onClickAdd = props.onAddItem
                }
            }


            Divider { }

            ListItem {
                ListItemButton{

                    ListItemText{
                        primary = ReactNode(strings[MR.strings.account])
                        secondary = ReactNode(props.uiState.version)
                    }
                }
            }

        }
    }

}

val AccountListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        AccountListViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(AccountListUiState())

    AccountListComponent2 {
        uiState = uiStateVal
        onLogoutClick = viewModel::onClickLogout
        onMyProfileClick = viewModel::onClickProfile
        onAddItem = viewModel::onClickAddAccount
        onAccountListItemClick = viewModel::onClickAccount
        onDeleteListItemClick = viewModel::onClickDeleteAccount
    }
}

val AccountListScreenPreview = FC<Props> {
    AccountListComponent2{
        uiState = AccountListUiState(
            headerAccount = UserSessionWithPersonAndEndpoint(
                userSession = UserSession().apply {
                },
                person = Person().apply {
                    firstNames = "Sara"
                    lastName = "Sarvari"
                    personUid = 9
                    username = "sara99"
                },
                endpoint = Endpoint(
                    url = "https://example.com"
                )
            ),
            accountsList = listOf(
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Ahmad"
                        lastName = "Ahmadi"
                        personUid = 4
                        username = "ahmadi"
                    },
                    endpoint = Endpoint(
                        url = "https://example.com"
                    )
                ),
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Negin"
                        lastName = "Naseri"
                        personUid = 5
                        username = "negin10"
                    },
                    endpoint = Endpoint(
                        url = "https://someweb.com"
                    )
                ),
                UserSessionWithPersonAndEndpoint(
                    userSession = UserSession().apply {
                    },
                    person = Person().apply {
                        firstNames = "Ali"
                        lastName = "Asadi"
                        personUid = 6
                        username = "ali01"
                    },
                    endpoint = Endpoint(
                        url = "https://thisisalink.org"
                    )
                )
            )
        )
    }
}