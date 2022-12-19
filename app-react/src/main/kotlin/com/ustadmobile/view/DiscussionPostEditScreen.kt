package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseDiscussionBlockEditUiState
import com.ustadmobile.core.viewmodel.DiscussionPostEditUiState
import com.ustadmobile.core.viewmodel.SiteEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadDateTimeEditField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadSwitchField
import csstype.px
import kotlinx.html.currentTimeMillis
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import react.create

external interface DiscussionPostEditProps: Props {
    var uiState: DiscussionPostEditUiState
}

val DiscussionPostEditComponent2 = FC<DiscussionPostEditProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)

            UstadTextEditField {
                value = props.uiState.discussionPost?.discussionPostTitle ?: ""
                label = strings[MessageID.title]
                error = props.uiState.discussionPostTitleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    //TODO
                }
            }

            UstadTextEditField {
                value = props.uiState.discussionPost?.discussionPostMessage ?: ""
                label = strings[MessageID.message]
                error = props.uiState.discussionPostDescError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    //TODO
                }
            }


            /*
                onChange = {
                    props.onSiteChanged(
                        props.uiState.site?.shallowCopy {
                            siteName = it
                        })
                }

                 */
        }

    }
}


val DiscussionPostEditPreview = FC<Props> {
    DiscussionPostEditComponent2 {
        uiState = DiscussionPostEditUiState(

            discussionPost = DiscussionPost().apply {
                discussionPostTitle = "How to submit report A?"
                discussionPostMessage =
                    "For our sales report, do I upload or share a link? "
                discussionPostVisible = true

            },




        )
    }
}
