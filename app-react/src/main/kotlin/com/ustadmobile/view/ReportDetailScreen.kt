package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.ReportDetailUiState
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.components.UstadQuickActionButton
import csstype.AlignItems
import csstype.JustifyContent
import csstype.px
import mui.icons.material.CheckBoxOutlined
import mui.icons.material.Delete
import mui.icons.material.Download
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create

external interface ReportDetailScreenProps : Props {

    var uiState: ReportDetailUiState

    var onClickExport: () -> Unit

    var onClickAddToDashboard: (ReportWithSeriesWithFilters?) -> Unit

    var onClickAddAsTemplate: (ReportWithSeriesWithFilters?) -> Unit

}

val ReportDetailScreenPreview = FC<Props> {
    ReportDetailScreenComponent2 {
        uiState = ReportDetailUiState(
            saveAsTemplateVisible = true
        )
    }
}

private val ReportDetailScreenComponent2 = FC<ReportDetailScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            QuickActionBars {
                uiState = props.uiState
                onClickExport = props.onClickExport
                onClickAddToDashboard = props.onClickAddToDashboard
                onClickAddAsTemplate = props.onClickAddAsTemplate
            }

            Stack {
                direction = responsive(StackDirection.row)
                justifyContent = JustifyContent.spaceBetween

                Typography {
                    + strings[MessageID.person]
                }

                Typography {
                    + strings[MessageID.xapi_verb_header]
                }

                Typography {
                    + strings[MessageID.xapi_result_header]
                }

                Typography {
                    + strings[MessageID.xapi_options_when]
                }
            }
        }
    }
}

private val QuickActionBars = FC <ReportDetailScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(5.px)
        sx {
            alignItems = AlignItems.center
        }

        UstadQuickActionButton {
            icon = CheckBoxOutlined.create()
            text = strings[MessageID.export].uppercase()
            onClick = { props.onClickExport }
        }

        if (props.uiState.addToDashboardVisible){
            UstadQuickActionButton {
                text = strings[MessageID.add_to].replace("%1\$s",
                    strings[MessageID.dashboard]).uppercase()
                icon = Delete.create()
                onClick = {
                    props.onClickAddToDashboard(props.uiState.chart?.reportWithFilters)
                }
            }
        }

        if (props.uiState.saveAsTemplateVisible){
            UstadQuickActionButton {
                text = strings[MessageID.save_as_template].uppercase()
                icon = Download.create()
                onClick = {
                    props.onClickAddAsTemplate(props.uiState.chart?.reportWithFilters)
                }
            }
        }
    }
}