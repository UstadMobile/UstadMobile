package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadQuickActionButton
import mui.icons.material.Call
import mui.icons.material.Email
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.*
import kotlin.js.Date

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
}

val PersonDetailPreview = FC<Props> {
    PersonDetailComponent2 {
        uiState = PersonDetailUiState(
            person = PersonWithPersonParentJoin().apply {
                firstNames = "Bob Jones"
            }
        )
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

                UstadQuickActionButton {
                    icon = Call.create()
                    text = strings[MessageID.call]
                }

                UstadQuickActionButton {
                    icon = Email.create()
                    text = strings[MessageID.email]
                }
            }

            UstadDetailField {
                icon = mui.icons.material.AccountCircle.create()
                labelText = strings[MessageID.name]
                valueText = props.uiState.person?.firstNames ?: ""
            }

            val birthdayFormatted = useMemo(dependencies = arrayOf(props.uiState.person?.dateOfBirth)) {
                Date(props.uiState.person?.dateOfBirth ?: 0L).toLocaleDateString()
            }

            UstadDetailField{
                icon = mui.icons.material.CalendarToday.create()
                labelText = strings[MessageID.birthday]
                valueText = birthdayFormatted
            }
        }

    }
}
