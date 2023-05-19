package com.ustadmobile.view

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.AccountListUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.mui.components.UstadAddListItem
import csstype.px
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.Delete
import mui.icons.material.AccountCircle
import mui.icons.material.Person2
import mui.icons.material.LinkOutlined
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode

external interface AccountListProps: Props {
    var uiState: AccountListUiState
    var onAccountListItemClick: (UserSessionWithPersonAndEndpoint?) -> Unit
    var onDeleteListItemClick: (UserSessionWithPersonAndEndpoint?) -> Unit
    var onAboutClick: () -> Unit
    var onAddItem: () -> Unit
    var onMyProfileClick: () -> Unit
    var onLogoutClick: () -> Unit
}

interface AccountListItemContentProps: Props{
    var account: UserSessionWithPersonAndEndpoint?
    var onDeleteListItemClick: ((UserSessionWithPersonAndEndpoint?) -> Unit)?
}

external interface AccountListItemProps: Props {
    var onListItemClick: ((UserSessionWithPersonAndEndpoint) -> Unit)?
    var account: UserSessionWithPersonAndEndpoint?
    var onDeleteListItemClick: ((UserSessionWithPersonAndEndpoint?) -> Unit)?
}

private val AccountListItemContent = FC<AccountListItemContentProps> { props ->

    if (props.onDeleteListItemClick != null){
        ListItemSecondaryAction{
            IconButton{
                onClick = {
                    props.onDeleteListItemClick?.invoke(props.account)
                }
                Delete()
            }
        }
    }

    ListItemIcon{
        Icon{
            sx{
                width = 40.px
                height = 40.px
            }
            AccountCircle{
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
            primary = ReactNode("${props.account?.person?.firstNames} ${props.account?.person?.lastName}")
        }

        Stack{
            direction = responsive(StackDirection.row)

            Icon{
                sx{
                    width = 20.px
                    height = 20.px
                }
                Person2{
                    sx{
                        width = 20.px
                        height = 20.px
                    }
                }
            }

            ListItemText{
                sx{
                    paddingRight = 20.px
                    paddingLeft = 5.px
                }
                secondary = ReactNode(props.account?.person?.username ?: "")
            }

            Icon{
                sx{
                    width = 20.px
                    height = 20.px
                }

                LinkOutlined{
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
                secondary = ReactNode(props.account?.endpoint?.url ?: "")
            }
        }
    }
}

val AccountListItem = FC<AccountListItemProps> {    props ->
    if(props.onListItemClick != null){
        ListItem{
            disablePadding = true

            ListItemButton{
                AccountListItemContent{
                    account = props.account
                    onDeleteListItemClick = props.onDeleteListItemClick
                }
            }
        }
    }else{
        ListItem{

            AccountListItemContent{
                account = props.account
            }
        }
    }
}

val AccountListComponent2 = FC<AccountListProps> {  props ->

    val strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            AccountListItem{
                account = props.uiState.activeAccount
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
                    + strings[MessageID.my_profile].uppercase()
                }

                Button {
                    onClick = { props.onLogoutClick() }
                    variant = ButtonVariant.outlined
                    + strings[MessageID.logout].uppercase()
                }
            }

            Divider{
                sx{
                    paddingTop = 10.px
                }
            }

            mui.material.List {
                props.uiState.accountsList.forEach { thisAccount ->
                    AccountListItem{
                        onListItemClick = {
                            props.onAccountListItemClick(props.uiState.activeAccount)
                        }
                        account = thisAccount
                        onDeleteListItemClick = {
                            props.onDeleteListItemClick(thisAccount)
                        }
                    }
                }

                UstadAddListItem {
                    text = strings[MessageID.add_another_account]
                    onClickAdd = props.onAddItem
                }
            }


            Divider{}

            ListItem{
                ListItemButton{
                    onClick = {
                        props.onAboutClick()
                    }

                    ListItemText{
                        primary = ReactNode(strings[MessageID.account])
                        secondary = ReactNode(props.uiState.version)
                    }
                }
            }

        }
    }

}

val AccountListScreenPreview = FC<Props> {
    AccountListComponent2{
        uiState = AccountListUiState(
            activeAccount = UserSessionWithPersonAndEndpoint(
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