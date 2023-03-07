package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.CourseGroupSetDetailUiState
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet
import csstype.px
import io.ktor.http.*
import mui.icons.material.AccountCircle
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode

external interface CourseGroupSetDetailProps: Props {
    var uiState: CourseGroupSetDetailUiState
}

val CourseGroupSetDetailComponent2 = FC<CourseGroupSetDetailProps> { props ->

    val strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            (1..(props.uiState.courseGroupSet?.cgsTotalGroups ?: 1)).map{ group ->
                Typography {

                    sx {
                        paddingLeft = 16.px
                    }

                    + "${strings[MessageID.group]} $group"
                    variant = TypographyVariant.body1
                }

                props.uiState.membersList.filter { it.cgm.cgmGroupNumber == group }.forEach { member ->
                    ListItem{
                        ListItemIcon{
                            AccountCircle()
                        }

                        ListItemText{
                            primary = ReactNode(member.name)
                        }
                    }
                }
            }

        }

    }

}

val CourseGroupSetDetailScreenPreview = FC<Props> {
    CourseGroupSetDetailComponent2{
        uiState = CourseGroupSetDetailUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "Group 1"
                cgsTotalGroups = 4
            },
            membersList = listOf(
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    }
}