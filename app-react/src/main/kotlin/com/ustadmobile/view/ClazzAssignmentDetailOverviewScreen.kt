package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionConstants
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.system.Stack
import mui.material.List
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.useMemo
import kotlin.js.Date

external interface ClazzAssignmentDetailOverviewProps : Props {
    var uiState: ClazzAssignmentDetailOverviewUiState
}

val ClazzAssignmentDetailOverviewPreview = FC<Props> {
    ClazzAssignmentDetailOverviewComponent2 {
        uiState = ClazzAssignmentDetailOverviewUiState(
            clazzAssignments = listOf(
                ClazzAssignmentWithCourseBlock().apply {
                    caDescription = "Read the stories and describe the main characters."
                    block = CourseBlock().apply {
                        cbDeadlineDate = 1668153441000
                    }
                }
            )
        )
    }
}

val ClazzAssignmentDetailOverviewComponent2 = FC<ClazzAssignmentDetailOverviewProps> { props ->

    val strings = useStringsXml()

    List{
        props.uiState.clazzAssignments.forEach {
            ListItem{
                Stack {
                    direction = responsive(StackDirection.column)
                    spacing = responsive(15.px)

                    val dateOfDeadLine = useMemo(dependencies = arrayOf(it.block?.cbDeadlineDate)) {
                        Date(it.block?.cbDeadlineDate ?: 0L).toLocaleDateString()
                    }

                    val cbDeadlineDateVisible: Boolean = it.block?.cbDeadlineDate.isDateSet()

                    val caSubmissionPolicy = strings.mapLookup(
                        it.caSubmissionPolicy,
                        SubmissionConstants.SUBMISSION_POLICY_OPTIONS
                    )

                    if (!it.caDescription.isNullOrBlank()) {
                        + (it.caDescription ?: "")
                    }

                    Box{
                        sx {
                           height = 10.px
                        }
                    }

                    if (cbDeadlineDateVisible){
                        UstadDetailField {
                            icon = EventAvailable.create()
                            labelText = strings[MessageID.deadline]
                            valueText = dateOfDeadLine
                        }
                    }


                    UstadDetailField {
                        icon = TaskAlt.create()
                        labelText = strings[MessageID.submission_policy]
                        valueText = caSubmissionPolicy ?: ""
                    }
                }
            }
        }
    }
}
