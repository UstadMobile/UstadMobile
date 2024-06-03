package com.ustadmobile.view.clazzassignment.submitterdetail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadNumberTextField
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import dev.icerock.moko.resources.StringResource
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.pct
import js.objects.jso
import mui.material.Box
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.InputAdornment
import mui.material.InputAdornmentPosition
import mui.material.InputBaseProps
import mui.material.Stack
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext

external interface CourseAssignmentMarkEditProps: Props {

    var draftMark: CourseAssignmentMark

    var maxPoints: Float

    var scoreError: String?

    var markFieldsEnabled: Boolean

    var onChangeDraftMark: (CourseAssignmentMark) -> Unit

    var onClickSubmitGrade: () -> Unit

    var onClickSubmitGradeAndMarkNext: () -> Unit

    var submitButtonLabelStringResource: StringResource

    var submitGradeButtonAndGoNextStringResource: StringResource

}

val CourseAssignmentMarkEdit = FC<CourseAssignmentMarkEditProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val strings = useStringProvider()

    Stack {
        spacing = responsive(theme.spacing(1))
        sx {
            paddingTop = theme.spacing(2)
        }

        UstadTextField {
            id = "marker_comment"
            label = ReactNode(strings[MR.strings.mark_comment])
            value = props.draftMark.camMarkerComment ?: ""
            disabled = !props.markFieldsEnabled
            onTextChange = {
                props.onChangeDraftMark(props.draftMark.shallowCopy {
                    camMarkerComment = it
                })
            }
            fullWidth = true
        }

        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.stretch
                width = 100.pct
            }

            UstadNumberTextField {
                id = "marker_mark"
                sx {
                    marginRight = theme.spacing(1)
                }
                numValue = props.draftMark.camMark
                numValueIfBlank = (-1).toFloat()
                label = ReactNode(strings[MR.strings.mark])
                onChange = {
                    props.onChangeDraftMark(props.draftMark.shallowCopy {
                        camMark = it
                    })
                }
                helperText = props.scoreError?.let { ReactNode(it) }
                error = props.scoreError != null
                disabled = !props.markFieldsEnabled

                asDynamic().InputProps = jso<InputBaseProps> {
                    endAdornment = InputAdornment.create {
                        position = InputAdornmentPosition.end
                        + "/${props.maxPoints} ${strings[MR.strings.points]}"
                    }
                }
                fullWidth = true
            }

            Button {
                id = "submit_mark_button"
                variant = ButtonVariant.contained
                onClick = {
                    props.onClickSubmitGrade()
                }
                fullWidth = true
                disabled = !props.markFieldsEnabled

                + strings[props.submitButtonLabelStringResource]
            }
        }

        /*
        To be enabled when reactive sync is introduced
        Button {
            id = "submit_and_mark_next"
            variant = ButtonVariant.outlined
            onClick = {
                props.onClickSubmitGradeAndMarkNext()
            }
            fullWidth = true

            + strings[props.submitGradeButtonAndGoNextMessageId]
        }
         */

    }
}