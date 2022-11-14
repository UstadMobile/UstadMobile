package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadQuickActionButton
import csstype.AlignContent
import csstype.Display
import mui.icons.material.*
import mui.material.*
import mui.material.Box
import mui.icons.material.Badge
import mui.material.Container
import mui.material.styles.TypographyVariant
import mui.system.*
import mui.system.Stack
import mui.system.StackDirection
import react.*
import kotlin.js.Date
import csstype.px

val PersonDetailScreen = FC<Props>() {
    val di = useContext(DIContext)

//    val viewModel = useViewModel { DummyViewModel() }
//
//    val dummyUiState: DummyUiState by viewModel.uiState.collectAsState(DummyUiState())
//
//    val statePerson: Person? by dummyUiState.personState.collectAsState(null)
//
//    PersonDetailComponent2 {
//        person = statePerson
//    }
}

external interface PersonDetailProps : Props {
    var uiState: PersonDetailUiState
    val clazzes: List<ClazzEnrolmentWithClazzAndAttendance>
    val onClickDial: () -> Unit
    val onClickSms: () -> Unit
    val onClickEmail: () -> Unit
    val onClickCreateAccount: () -> Unit
    val onClickChangePassword: () -> Unit
    val onClickManageParentalConsent: () -> Unit
    val onClickChat: () -> Unit
    val onClickClazz: () -> Unit
}

val PersonDetailPreview = FC<Props> {
    PersonDetailComponent2 {
        uiState = PersonDetailUiState()
    }
}

val PersonDetailComponent2 = FC<PersonDetailProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)

            //Quick action bar here
            Stack {
                direction = responsive(StackDirection.row)

                if (!props.uiState.person?.phoneNum.isNullOrEmpty()) {
                    UstadQuickActionButton {
                        icon = Call.create()
                        text = strings[MessageID.call]
                        onClick = { props.onClickDial }
                    }

                    UstadQuickActionButton {
                        icon = Sms.create()
                        text = strings[MessageID.text]
                        onClick = { props.onClickSms }
                    }
                }

                if (!props.uiState.person?.emailAddr.isNullOrEmpty()) {
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
                        onClick = { props.onClickCreateAccount}
                    }
                }

                if (props.uiState.changePasswordVisible) {
                    UstadQuickActionButton {
                        icon = Key.create()
                        text = strings[MessageID.change_password]
                        onClick = { props.onClickChangePassword }
                    }
                }

                if (props.uiState.person?.parentJoin != null) {
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

            Divider { orientation = Orientation.horizontal }

            Box {
                sx {
                    height = 10.px
                }
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.basic_details]
            }

            if (props.uiState.person?.dateOfBirth != 0L
                && props.uiState.person?.dateOfBirth != null){
                val birthdayFormatted = useMemo(dependencies = arrayOf(props.uiState.person?.dateOfBirth)) {
                    Date(props.uiState.person?.dateOfBirth ?: 0L).toLocaleDateString()
                }

                UstadDetailField {
                    icon = CalendarToday.create()
                    labelText = strings[MessageID.birthday]
                    valueText = birthdayFormatted
                }
            }

            if (props.uiState.person?.gender != 0
                && props.uiState.person?.gender != null){

                UstadDetailField {
                    icon = null
                    labelText = strings[MessageID.gender_literal]
                    valueText = props.uiState.person?.firstNames ?: ""
                }
            }

            if (props.uiState.person?.personOrgId != null){
                UstadDetailField {
                    icon = Badge.create()
                    labelText = strings[MessageID.organization_id]
                    valueText = props.uiState.person?.personOrgId ?: ""
                }
            }

            if (!props.uiState.person?.username.isNullOrEmpty()){
                UstadDetailField {
                    icon = AccountCircle.create()
                    labelText = strings[MessageID.username]
                    valueText = props.uiState.person?.username ?: ""
                }
            }

            Divider { orientation = Orientation.horizontal }

            Box {
                sx {
                    height = 10.px
                }
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.contact_details]
            }

            if (!props.uiState.person?.phoneNum.isNullOrEmpty()){
                Stack{
                    direction = responsive(StackDirection.row)

                    UstadDetailField {
                        icon = Call.create()
                        labelText = strings[MessageID.phone]
                        valueText = props.uiState.person?.phoneNum ?: ""
                    }

                    + Message.create()
                }
            }

            if (!props.uiState.person?.emailAddr.isNullOrEmpty()){
                UstadDetailField {
                    icon = Email.create()
                    labelText = strings[MessageID.email]
                    valueText = props.uiState.person?.emailAddr ?: ""
                }
            }

            if (!props.uiState.person?.personAddress.isNullOrEmpty()){
                UstadDetailField {
                    icon = LocationOn.create()
                    labelText = strings[MessageID.address]
                    valueText = props.uiState.person?.personAddress ?: ""
                }
            }

            Divider { orientation = Orientation.horizontal }

            Box {
                sx {
                    height = 10.px
                }
            }

            Typography {
                variant = TypographyVariant.h6
                + strings[MessageID.classes]
            }


        }
    }
}
