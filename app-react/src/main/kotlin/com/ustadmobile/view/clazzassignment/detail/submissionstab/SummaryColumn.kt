package com.ustadmobile.view.clazzassignment.detail.submissionstab

import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.mui.components.ThemeContext
import csstype.Border
import csstype.LineStyle
import csstype.Padding
import csstype.TextAlign
import csstype.px
import mui.material.Stack
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.useRequiredContext

external interface ClazzAssignmentSummaryColumnProps : Props {

    var total: Int?

    var label: String

    var showDivider: Boolean?

}

val ClazzAssignmentSummaryColumn = FC<ClazzAssignmentSummaryColumnProps> { props ->

    val theme by useRequiredContext(ThemeContext)

    Stack {
        sx {
            if(props.showDivider == true)
                borderRight = Border(1.px, LineStyle.solid, theme.palette.divider)

            padding = Padding(horizontal = 32.px, vertical = 8.px)
            textAlign = TextAlign.center
        }

        Typography {
            variant = TypographyVariant.h4
            + (props.total).toString()
        }

        Typography {
            + props.label.capitalizeFirstLetter()
        }
    }

}

