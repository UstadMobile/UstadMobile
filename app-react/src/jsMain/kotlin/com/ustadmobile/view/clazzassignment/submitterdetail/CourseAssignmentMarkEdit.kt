package com.ustadmobile.view.clazzassignment.submitterdetail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadNumberTextField
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.util.ext.onTextChange
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.pct
import js.core.jso
import mui.material.Box
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.InputAdornment
import mui.material.InputAdornmentPosition
import mui.material.InputBaseProps
import mui.material.Stack
import mui.material.TextField
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext

external interface CourseAssignmentMarkEditProps: Props {

    var draftMark: CourseAssignmentMark

    var maxPoints: Int

    var scoreError: String?

    var onChangeDraftMark: (CourseAssignmentMark) -> Unit

    var onClickSubmitGrade: () -> Unit

    var onClickSubmitGradeAndMarkNext: () -> Unit

    var submitButtonLabelMessageId: Int

    var submitGradeButtonAndGoNextMessageId: Int

}

val CourseAssignmentMarkEdit = FC<CourseAssignmentMarkEditProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val strings = useStringsXml()

    Stack {
        spacing = responsive(theme.spacing(1))
        sx {
            paddingTop = theme.spacing(2)
        }

        TextField {
            id = "marker_comment"
            label = ReactNode(strings[MessageID.mark_comment].addOptionalSuffix(strings))
            value = props.draftMark.camMarkerComment ?: ""
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
                label = ReactNode(strings[MessageID.mark])
                onChange = {
                    props.onChangeDraftMark(props.draftMark.shallowCopy {
                        camMark = it
                    })
                }
                helperText = props.scoreError?.let { ReactNode(it) }
                error = props.scoreError != null

                asDynamic().InputProps = jso<InputBaseProps> {
                    endAdornment = InputAdornment.create {
                        position = InputAdornmentPosition.end
                        + "/${props.maxPoints} ${strings[MessageID.points]}"
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

                + strings[props.submitButtonLabelMessageId]
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