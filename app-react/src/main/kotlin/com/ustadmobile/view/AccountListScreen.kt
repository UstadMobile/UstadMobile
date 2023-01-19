package com.ustadmobile.view

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.AccountListUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import csstype.px
import mui.icons.material.*
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

val AccountListComponent2 = FC<AccountListProps> {  props ->

    var strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            ListItem{

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
                        primary = ReactNode("${props.uiState.activeAccount?.person?.firstNames} ${props.uiState.activeAccount?.person?.lastName}")
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
                            secondary = ReactNode(props.uiState.activeAccount?.person?.username ?: "")
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
                            secondary = ReactNode(props.uiState.activeAccount?.endpoint?.url ?: "")
                        }
                    }
                }

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

            props.uiState.accountsList.forEach { account ->
                ListItem{

                    disablePadding = true

                    ListItemButton{

                        onClick = {
                            props.onAccountListItemClick(account)
                        }

                        ListItemSecondaryAction{
                            IconButton{
                                onClick = {
                                    props.onDeleteListItemClick(account)
                                }
                                Delete()
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
                                primary = ReactNode("${account.person.firstNames} ${account.person.lastName}")
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
                                    secondary = ReactNode(account.person.username ?: "")
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
                                    secondary = ReactNode(account.endpoint.url)
                                }
                            }
                        }
                    }

                }
            }

            ListItem{
                ListItemButton{

                    onClick = {
                        props.onAddItem()
                    }

                    ListItemIcon {
                        Add()
                    }

                    ListItemText{
                        primary = ReactNode(strings[MessageID.add_another_account])
                    }
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