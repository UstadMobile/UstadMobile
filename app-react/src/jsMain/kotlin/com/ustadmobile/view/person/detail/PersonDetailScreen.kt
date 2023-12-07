package com.ustadmobile.view.person.detail

import com.ustadmobile.core.viewmodel.person.detail.PersonDetailUiState
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.mapLookup
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.hooks.useAttachmentUriSrc
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadQuickActionButton
import com.ustadmobile.view.components.UstadFab
import web.cssom.ObjectFit
import mui.material.List
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.material.*
import mui.icons.material.Call
import mui.icons.material.Email
import mui.icons.material.Key
import mui.icons.material.SupervisedUserCircle
import mui.icons.material.Chat
import mui.icons.material.CalendarToday
import mui.icons.material.AccountCircle
import mui.icons.material.LocationOn
import mui.icons.material.Person
import mui.icons.material.People
import react.dom.html.ReactHTML.img
import mui.icons.material.Badge
import mui.material.Container
import mui.material.styles.TypographyVariant
import mui.system.*
import mui.system.Stack
import mui.system.StackDirection
import react.*
import kotlin.js.Date
import web.cssom.px
import emotion.react.css

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
    }

}

external interface PersonDetailProps : Props {
    var uiState: PersonDetailUiState
    var clazzes: List<Clazz>
    var onClickDial: () -> Unit
    var onClickEmail: () -> Unit
    var onClickCreateAccount: () -> Unit
    var onClickChangePassword: () -> Unit
    var onClickManageParentalConsent: () -> Unit
    var onClickChat: () -> Unit
}

val PersonDetailPreview = FC<Props> {
    PersonDetailComponent2 {
        uiState = PersonDetailUiState(
            person = PersonWithPersonParentJoin().apply {
                firstNames = "Bob Jones"
                phoneNum = "0799999"
                emailAddr = "Bob@gmail.com"
                gender = 1
                username = "bob12"
                dateOfBirth = 12
                personOrgId = "123"
                personAddress = "Herat"
            },
            chatVisible = true,
            clazzes = listOf(
                ClazzEnrolmentWithClazzAndAttendance().apply {
                    clazz = Clazz().apply {
                        clazzName = "Jetpack Compose Class"
                    }
                },
                ClazzEnrolmentWithClazzAndAttendance().apply {
                    clazz = Clazz().apply {
                        clazzName = "React Class"
                    }
                },
            )
        )
    }
}


val PersonDetailComponent2 = FC<PersonDetailProps> { props ->

    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            val personImgSrc = useAttachmentUriSrc(
                attachmentUri = props.uiState.personPicture?.personPictureUri,
                revokeOnCleanup = true,
            )

            if(personImgSrc != null) {
                img {
                    src = personImgSrc.toString()
                    alt = "user image"
                    css {
                        maxHeight = 304.px
                        objectFit = ObjectFit.contain
                        asDynamic().objectPosition = "center"
                    }
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
                icon = Call.create()
                text = strings[MR.strings.call]
                onClick = {
                    props.onClickDial()
                }
            }
        }

        if (props.uiState.emailVisible) {
            UstadQuickActionButton {
                icon = Email.create()
                text = strings[MR.strings.email]
                onClick = {
                    props.onClickEmail()
                }
            }
        }

        if (props.uiState.showCreateAccountVisible) {
            UstadQuickActionButton {
                icon = Person.create()
                text = strings[MR.strings.create_account]
                onClick = {
                    props.onClickCreateAccount()
                }
            }
        }

        if (props.uiState.changePasswordVisible) {
            UstadQuickActionButton {
                icon = Key.create()
                text = strings[MR.strings.change_password]
                onClick = { props.onClickChangePassword() }
            }
        }

        if (props.uiState.manageParentalConsentVisible) {
            UstadQuickActionButton {
                icon = SupervisedUserCircle.create()
                text = strings[MR.strings.manage_parental_consent]
                onClick = { props.onClickManageParentalConsent() }
            }
        }

        if (props.uiState.chatVisible) {
            UstadQuickActionButton {
                icon = Chat.create()
                text = strings[MR.strings.chat]
                onClick = { props.onClickChat() }
            }
        }
    }
}

private val DetailFeilds = FC<PersonDetailProps> { props ->

    val strings = useStringProvider()

    val birthdayFormatted = useMemo(dependencies = arrayOf(props.uiState.person?.dateOfBirth)) {
        Date(props.uiState.person?.dateOfBirth ?: 0L).toLocaleDateString()
    }

    if (props.uiState.dateOfBirthVisible){
        UstadDetailField {
            icon = CalendarToday.create()
            labelText = strings[MR.strings.birthday]
            valueText = ReactNode(birthdayFormatted)
        }
    }

    if (props.uiState.personGenderVisible){

        UstadDetailField {
            icon = null
            labelText = strings[MR.strings.gender_literal]
            valueText = ReactNode(strings.mapLookup(
                props.uiState.person?.gender ?: 1,
                PersonConstants.GENDER_MESSAGE_ID_MAP
            ))
        }
    }

    if (props.uiState.personOrgIdVisible){
        UstadDetailField {
            icon = Badge.create()
            labelText = strings[MR.strings.organization_id]
            valueText = ReactNode(props.uiState.person?.personOrgId ?: "")
        }
    }

    if (props.uiState.personUsernameVisible){
        UstadDetailField {
            icon = AccountCircle.create()
            labelText = strings[MR.strings.username]
            valueText = ReactNode(props.uiState.person?.username ?: "")
        }
    }
}

private val ContactDetails = FC<PersonDetailProps> { props ->

    val strings = useStringProvider()

    if (props.uiState.phoneNumVisible){
        UstadDetailField{
            valueText = ReactNode(props.uiState.displayPhoneNum ?: props.uiState.person?.phoneNum ?: "")
            labelText = strings[MR.strings.phone]
            icon = Call.create()
            onClick = props.onClickDial
        }
    }

    if (props.uiState.emailVisible){
        UstadDetailField {
            icon = Email.create()
            labelText = strings[MR.strings.email]
            valueText = ReactNode(props.uiState.person?.emailAddr ?: "")
        }
    }

    if (props.uiState.personAddressVisible){
        UstadDetailField {
            icon = LocationOn.create()
            labelText = strings[MR.strings.address]
            valueText = ReactNode(props.uiState.person?.personAddress ?: "")
        }
    }
}

private val Classes = FC<PersonDetailProps> { props ->

    List{
        props.uiState.clazzes.forEach {
            ListItem{
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(10.px)

                    + People.create()

                    Typography {
                        align = TypographyAlign.center
                        + (it.clazz?.clazzName ?: "")
                    }
                }
            }
        }
    }
}
