package com.ustadmobile.view.report

import com.ustadmobile.core.domain.report.model.ReportFilter2
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.ConditionConstants
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.person.edit.PersonEditComponent2
import kotlinx.coroutines.Dispatchers
import web.cssom.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import web.html.InputMode

external interface ReportFilterEditScreenProps : Props {
    var uiState: ReportFilterEditUiState
    var onReportFilterChanged: (ReportFilter2?) -> Unit
}

private val ReportFilterEditScreenComponent2 = FC<ReportFilterEditScreenProps> { props ->
    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadMessageIdSelectField {
                value = 0
                label = ""
                options = FieldConstants.FIELD_MESSAGE_IDS
                onChange = {
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                UstadMessageIdSelectField {
                    value = 0
                    label = ""
                    options = ConditionConstants.CONDITION_MESSAGE_IDS
                    onChange = {
                    }
                }
            }

            UstadTextEditField {
                value = ""
                label = ""
                onChange = {
                }
            }
        }
    }
}
val ReportFilterEditScreenComponent = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportFilterEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(
        ReportFilterEditUiState(), Dispatchers.Main.immediate)

    ReportFilterEditScreenComponent2 {
        uiState = uiStateVar
        onReportFilterChanged = viewModel::onEntityChanged
    }
}
