package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.PersonDetailViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadQuickActionButton
import com.ustadmobile.view.components.UstadFab
import mui.material.List
import mui.icons.material.*
import mui.material.*
import react.dom.html.ReactHTML.img
import mui.icons.material.Badge
import mui.material.Container
import mui.material.styles.TypographyVariant
import mui.system.*
import mui.system.Stack
import mui.system.StackDirection
import react.*
import kotlin.js.Date
import csstype.px

val PersonDetailScreen = FC<UstadScreenProps>() { props ->
    val viewModel = useUstadViewModel(
        onAppUiStateChange = props.onAppUiStateChanged,
        onShowSnack = props.onShowSnackBar,
    ) { di, savedStateHandle ->
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

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            img {
                src = "${""}?w=164&h=164&fit=crop&auto=format"
                alt = "user image"
            }

            QuickActionBar{
                uiState = props.uiState
            }

            Divider { orientation = Orientation.horizontal }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.basic_details]
            }

            DetailFeilds{
                uiState = props.uiState
            }

            Divider { orientation = Orientation.horizontal }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.contact_details]
            }

            ContactDetails{
                uiState = props.uiState
            }

            Divider { orientation = Orientation.horizontal }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.classes]
            }

            Classes{
                uiState = props.uiState
            }
        }
    }
}

private val QuickActionBar = FC<PersonDetailProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)

        if (props.uiState.phoneNumVisible) {
            UstadQuickActionButton {
                icon = Call.create()
                text = strings[MessageID.call]
                onClick = { props.onClickDial }
            }
        }

        if (props.uiState.emailVisible) {
            UstadQuickActionButton {
                icon = Email.create()
                text = strings[MessageID.email]
                onClick = { props.onClickEmail }
            }
        }

        if (props.uiState.showCreateAccountVisible) {
            UstadQuickActionButton {
                icon = Person.create()
                text = strings[MessageID.create_account]
                onClick = { props.onClickCreateAccount }
            }
        }

        if (props.uiState.changePasswordVisible) {
            UstadQuickActionButton {
                icon = Key.create()
                text = strings[MessageID.change_password]
                onClick = { props.onClickChangePassword }
            }
        }

        if (props.uiState.manageParentalConsentVisible) {
            UstadQuickActionButton {
                icon = SupervisedUserCircle.create()
                text = strings[MessageID.manage_parental_consent]
                onClick = { props.onClickManageParentalConsent }
            }
        }

        if (props.uiState.chatVisible) {
            UstadQuickActionButton {
                icon = Chat.create()
                text = strings[MessageID.chat]
                onClick = { props.onClickChat }
            }
        }
    }
}

private val DetailFeilds = FC<PersonDetailProps> { props ->

    val strings = useStringsXml()

    val birthdayFormatted = useMemo(dependencies = arrayOf(props.uiState.person?.dateOfBirth)) {
        Date(props.uiState.person?.dateOfBirth ?: 0L).toLocaleDateString()
    }

    if (props.uiState.dateOfBirthVisible){
        UstadDetailField {
            icon = CalendarToday.create()
            labelText = strings[MessageID.birthday]
            valueText = ReactNode(birthdayFormatted)
        }
    }

    if (props.uiState.personGenderVisible){

        val gender = strings.mapLookup(
            props.uiState?.person?.gender ?: 1,
            PersonConstants.GENDER_MESSAGE_ID_MAP
        )

        UstadDetailField {
            icon = null
            labelText = strings[MessageID.gender_literal]
            valueText = ReactNode(gender ?: "")
        }
    }

    if (props.uiState.personOrgIdVisible){
        UstadDetailField {
            icon = Badge.create()
            labelText = strings[MessageID.organization_id]
            valueText = ReactNode(props.uiState.person?.personOrgId ?: "")
        }
    }

    if (props.uiState.personUsernameVisible){
        UstadDetailField {
            icon = AccountCircle.create()
            labelText = strings[MessageID.username]
            valueText = ReactNode(props.uiState.person?.username ?: "")
        }
    }
}

private val ContactDetails = FC<PersonDetailProps> { props ->

    val strings = useStringsXml()

    if (props.uiState.phoneNumVisible){
        UstadDetailField{
            valueText = ReactNode(props.uiState.person?.phoneNum ?: "")
            labelText = strings[MessageID.phone]
            icon = Call.create()
        }
    }

    if (props.uiState.emailVisible){
        UstadDetailField {
            icon = Email.create()
            labelText = strings[MessageID.email]
            valueText = ReactNode(props.uiState.person?.emailAddr ?: "")
        }
    }

    if (props.uiState.personAddressVisible){
        UstadDetailField {
            icon = LocationOn.create()
            labelText = strings[MessageID.address]
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
