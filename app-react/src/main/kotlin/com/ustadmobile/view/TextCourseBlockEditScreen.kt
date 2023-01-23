package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.TextCourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.mui.components.UstadTimeEditField
import com.ustadmobile.util.ext.addOptionalSuffix
import csstype.px
import kotlinx.datetime.TimeZone
import mui.material.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface TextCourseBlockEditScreenProps : Props {

    var uiState: TextCourseBlockEditUiState

    var onBlockChanged: (CourseBlock?) -> Unit

    var onStartDateChanged: (Long?) -> Unit

    var onStartTimeChanged: (Int?) -> Unit

}

val TextCourseBlockEditScreenComponent2 = FC<TextCourseBlockEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)


            UstadTextEditField{
                value = props.uiState.block?.cbTitle ?: ""
                label = strings[MessageID.title]
                error = props.uiState.blockTitleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onBlockChanged(props.uiState.block?.shallowCopy{
                        cbTitle = it
                    })
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                UstadDateEditField {
                    fullWidth = true
                    timeInMillis = props.uiState.startDate
                    label = strings[MessageID.dont_show_before].addOptionalSuffix(strings)
                    enabled = props.uiState.fieldsEnabled
                    timeZoneId = TimeZone.currentSystemDefault().id
                    onChange = {
                        props.onStartDateChanged(it)
                    }
                }

                UstadTimeEditField {
                    fullWidth = true
                    timeInMillis = props.uiState.startTime
                    label = strings[MessageID.time]
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onStartTimeChanged(it)
                    }
                }
            }
        }
    }
}

val TextCourseBlockEditScreenPreview = FC<Props> {

    val uiStateVar : TextCourseBlockEditUiState by useState {
        TextCourseBlockEditUiState(

        )
    }

    TextCourseBlockEditScreenComponent2 {
        uiState = uiStateVar
    }
}