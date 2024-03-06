package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadRawHtml
import kotlinx.datetime.TimeZone
import mui.material.Box
import mui.system.sx
import react.FC
import react.Props
import react.useRequiredContext
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.capitalizeFirstLetter

external interface CourseAssignmentSubmissionProps: Props {
    var submission: CourseAssignmentSubmission
}

val CourseAssignmentSubmissionComponent = FC<CourseAssignmentSubmissionProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val strings = useStringProvider()
    val submittedTime = useFormattedDateAndTime(
        timeInMillis = props.submission.casTimestamp,
        timezoneId = TimeZone.currentSystemDefault().id,
    )


    Box {
        sx {
            padding = theme.spacing(2)
        }

        Stack {
            direction = responsive(StackDirection.column)

            UstadRawHtml {
                html = props.submission.casText ?: ""
            }

            Typography {
                variant = TypographyVariant.caption
                + "${strings[MR.strings.submitted_key].capitalizeFirstLetter()}: $submittedTime"
            }
        }

    }


}