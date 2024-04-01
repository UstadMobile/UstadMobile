package com.ustadmobile.view.person.detail

import com.ustadmobile.core.viewmodel.person.detail.PersonDetailUiState
import com.ustadmobile.lib.db.entities.PersonAndDisplayDetail
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.mapLookup
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadQuickActionButton
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTransferStatusIcon
import com.ustadmobile.view.components.UstadFab
import js.objects.jso
import mui.material.List
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.material.*
import mui.icons.material.Call as CallIcon
import mui.icons.material.Email as EmailIcon
import mui.icons.material.Key as KeyIcon
import mui.icons.material.SupervisedUserCircle as SupervisedUserCircleIcon
import mui.icons.material.Chat as ChatIcon
import mui.icons.material.CalendarToday as CalendarTodayIcon
import mui.icons.material.AccountCircle as AccountCircleIcon
import mui.icons.material.LocationOn as LocationOnIcon
import mui.icons.material.Person as PersonIcon
import mui.icons.material.People as PeopleIcon
import mui.material.styles.TypographyVariant
import mui.system.*
import mui.system.Stack
import mui.system.StackDirection
import react.*
import web.cssom.Display
import kotlin.js.Date
import web.cssom.px
import web.cssom.JustifyContent
import mui.icons.material.Shield as ShieldIcon

val PersonDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        PersonDetailViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(PersonDetailUiState())

    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }


    PersonDetailComponent2 {
        this.uiState = uiState
        onClickChat = viewModel::onClickChat
        onClickChangePassword = viewModel::onClickChangePassword
        onClickCreateAccount = viewModel::onClickCreateAccount
        onClickManageParentalConsent = viewModel::onClickManageParentalConsent
        onClickDial = viewModel::onClickDial
        onClickEmail = viewModel::onClickEmail
        onClickPermissions = viewModel::onClickPermissions
    }

}

external interface PersonDetailProps : Props {
    var uiState: PersonDetailUiState
    var onClickDial: () -> Unit
    var onClickEmail: () -> Unit
    var onClickCreateAccount: () -> Unit
    var onClickChangePassword: () -> Unit
    var onClickManageParentalConsent: () -> Unit
    var onClickChat: () -> Unit
    var onClickPermissions: () -> Unit
}

val PersonDetailPreview = FC<Props> {
    PersonDetailComponent2 {
        uiState = PersonDetailUiState(
            person = PersonAndDisplayDetail().apply {
                person = Person().apply {
                    firstNames = "Bob Jones"
                    phoneNum = "0799999"
                    emailAddr = "Bob@gmail.com"
                    gender = 1
                    username = "bob12"
                    dateOfBirth = 12
                    personOrgId = "123"
                    personAddress = "Herat"
                }
            },
            clazzes = listOf()
        )
    }
}


val PersonDetailComponent2 = FC<PersonDetailProps> { props ->

    val strings = useStringProvider()
    val pendingTransfer = props.uiState.person?.personPictureTransferJobItem

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)


            props.uiState.person?.personPicture?.personPictureUri?.also { imgSrc ->
                mui.material.Box {
                    sx {
                        justifyContent = JustifyContent.center
                        display = Display.flex
                    }

                    Badge {
                        overlap = BadgeOverlap.rectangular
                        anchorOrigin = jso {
                            vertical = BadgeOriginVertical.bottom
                            horizontal = BadgeOriginHorizontal.right
                        }
                        invisible = pendingTransfer == null
                        badgeContent = if(pendingTransfer != null) {
                            UstadTransferStatusIcon.create {
                                transferJobItemStatus = TransferJobItemStatus.valueOf(pendingTransfer.tjiStatus)
                                fontSize = SvgIconSize.small
                                color = SvgIconColor.action
                            }
                        }else {
                            ReactNode("")
                        }

                        Avatar {
                            sx {
                                width = 304.px
                                height = 304.px
                            }

                            src = imgSrc
                            alt = "user image"
                        }
                    }
                }

                Divider {
                    orientation = Orientation.horizontal
                }

            }

            QuickActionBar {
                + props
            }

            Divider { orientation = Orientation.horizontal }

            Typography {
                variant = TypographyVariant.h6
                + strings[MR.strings.basic_details]
            }

            DetailFeilds{
                uiState = props.uiState
            }

            Divider { orientation = Orientation.horizontal }

            Typography {
                variant = TypographyVariant.h6
                + strings[MR.strings.contact_details]
            }

            ContactDetails{
                uiState = props.uiState
            }

            Divider { orientation = Orientation.horizontal }

            Typography {
                variant = TypographyVariant.h6
                + strings[MR.strings.classes]
            }

            Classes{
                uiState = props.uiState
            }
        }
    }
}

private val QuickActionBar = FC<PersonDetailProps> { props ->

    val strings = useStringProvider()

    Stack {
        direction = responsive(StackDirection.row)

        if (props.uiState.phoneNumVisible) {
            UstadQuickActionButton {
                icon = CallIcon.create()
                text = strings[MR.strings.call]
                onClick = {
                    props.onClickDial()
                }
            }
        }

        if (props.uiState.emailVisible) {
            UstadQuickActionButton {
                icon = EmailIcon.create()
                text = strings[MR.strings.email]
                onClick = {
                    props.onClickEmail()
                }
            }
        }

        if (props.uiState.showCreateAccountVisible) {
            UstadQuickActionButton {
                icon = PersonIcon.create()
                text = strings[MR.strings.create_account]
                onClick = {
                    props.onClickCreateAccount()
                }
            }
        }

        if (props.uiState.changePasswordVisible) {
            UstadQuickActionButton {
                icon = KeyIcon.create()
                text = strings[MR.strings.change_password]
                onClick = { props.onClickChangePassword() }
            }
        }

        if(props.uiState.showPermissionButton) {
            UstadQuickActionButton {
                icon = ShieldIcon.create()
                text = strings[MR.strings.permissions]
                onClick = { props.onClickPermissions() }
            }
        }

        if (props.uiState.manageParentalConsentVisible) {
            UstadQuickActionButton {
                icon = SupervisedUserCircleIcon.create()
                text = strings[MR.strings.manage_parental_consent]
                onClick = { props.onClickManageParentalConsent() }
            }
        }

        if (props.uiState.chatVisible) {
            UstadQuickActionButton {
                icon = ChatIcon.create()
                text = strings[MR.strings.chat]
                onClick = { props.onClickChat() }
            }
        }
    }
}

private val DetailFeilds = FC<PersonDetailProps> { props ->

    val strings = useStringProvider()

    val birthdayFormatted = useMemo(dependencies = arrayOf(props.uiState.person?.person?.dateOfBirth)) {
        Date(props.uiState.person?.person?.dateOfBirth ?: 0L).toLocaleDateString()
    }

    if (props.uiState.dateOfBirthVisible){
        UstadDetailField {
            icon = CalendarTodayIcon.create()
            labelText = strings[MR.strings.birthday]
            valueText = ReactNode(birthdayFormatted)
        }
    }

    if (props.uiState.personGenderVisible){

        UstadDetailField {
            icon = null
            labelText = strings[MR.strings.gender_literal]
            valueText = ReactNode(strings.mapLookup(
                props.uiState.person?.person?.gender ?: 1,
                PersonConstants.GENDER_MESSAGE_ID_MAP
            ))
        }
    }

    if (props.uiState.personOrgIdVisible){
        UstadDetailField {
            icon = Badge.create()
            labelText = strings[MR.strings.organization_id]
            valueText = ReactNode(props.uiState.person?.person?.personOrgId ?: "")
        }
    }

    if (props.uiState.personUsernameVisible){
        UstadDetailField {
            icon = AccountCircleIcon.create()
            labelText = strings[MR.strings.username]
            valueText = ReactNode(props.uiState.person?.person?.username ?: "")
        }
    }
}

private val ContactDetails = FC<PersonDetailProps> { props ->

    val strings = useStringProvider()

    if (props.uiState.phoneNumVisible){
        UstadDetailField{
            valueText = ReactNode(props.uiState.displayPhoneNum ?: props.uiState.person?.person?.phoneNum ?: "")
            labelText = strings[MR.strings.phone]
            icon = CallIcon.create()
            onClick = props.onClickDial
        }
    }

    if (props.uiState.emailVisible){
        UstadDetailField {
            icon = EmailIcon.create()
            labelText = strings[MR.strings.email]
            valueText = ReactNode(props.uiState.person?.person?.emailAddr ?: "")
        }
    }

    if (props.uiState.personAddressVisible){
        UstadDetailField {
            icon = LocationOnIcon.create()
            labelText = strings[MR.strings.address]
            valueText = ReactNode(props.uiState.person?.person?.personAddress ?: "")
        }
    }
}

private val Classes = FC<PersonDetailProps> { props ->
    List {
        props.uiState.clazzes.forEach {
            ListItem {
                key = "${it.enrolment?.clazzEnrolmentUid}"

                ListItemButton {
                    ListItemIcon {
                        PeopleIcon()
                    }

                    ListItemText {
                        primary = ReactNode(it.clazz?.clazzName ?: "")
                    }
                }
            }
        }
    }
}
