package com.ustadmobile.mui.components

import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconSize
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.PropsWithSx
import mui.system.responsive
import mui.system.sx
import react.FC
import react.useRequiredContext
import web.cssom.px

external interface UstadCourseBlockHeaderProps : PropsWithSx {

    var block: CourseBlock?

    var picture: CourseBlockPicture?

    var id: String?

}

val UstadCourseBlockHeader = FC<UstadCourseBlockHeaderProps> { props ->
    val theme by useRequiredContext(ThemeContext)

    Stack {
        direction = responsive(StackDirection.row)
        sx = props.sx
        id = props.id

        UstadBlockIcon {
            courseBlock = props.block
            pictureUri = props.picture?.cbpPictureUri
            title = props.block?.cbTitle ?: ""
            width = 100.px
            height = 100.px
            iconSize = SvgIconSize.large
        }

        Typography {
            variant = TypographyVariant.h4
            id = "${props.id ?: "courseblock"}_title"
            sx {
                paddingLeft = theme.spacing(1)
            }

            + (props.block?.cbTitle ?: "")
        }
    }
}
