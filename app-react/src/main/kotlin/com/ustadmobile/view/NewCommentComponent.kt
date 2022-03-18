package com.ustadmobile.view

import Breakpoint
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.DefaultNewCommentItemListener
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import com.ustadmobile.mui.components.*
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.padding
import kotlinx.css.width
import react.RBuilder
import react.RComponent
import react.setState
import styled.css
import styled.styledSpan
import kotlin.js.Date

interface CommentProps: UmProps {
    var listener: DefaultNewCommentItemListener?
    var systemImpl: UstadMobileSystemImpl
    var shownAt: Long
    var person: Person?
    var title: String?
    var label: String?
    var onDialogClosed: () -> Unit
}

class NewCommentsComponent(mProps: CommentProps): RComponent<CommentProps, UmState>(mProps) {

    private var showDialog: Boolean = true

    private var lastShownAt = 0L

    private var classCommentLabel = FieldLabel(text = props.label)

    private var classCommentText = ""

    override fun RBuilder.render() {
        umDialog(showDialog,
            onClose = {handleDialogClosed()},
            fullWidth = true,
            maxWidth = Breakpoint.sm) {
            umDialogContent {
                css {
                    width = LinearDimension("100%")
                }
                umGridContainer(rowSpacing = GridSpacing.spacing4) {
                    css {
                        width = LinearDimension("100%")
                    }

                    if(props.person != null){
                        umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                            styledSpan {
                                css{
                                    padding(right = 2.spacingUnits)
                                }
                                umProfileAvatar(props.person?.personUid ?: 0,fallback = "person")
                                umTypography(props.person?.fullName())
                            }
                        }
                    }

                    umItem(GridSize.cells12){
                        umTextFieldMultiLine(
                            label = "${classCommentLabel.text}",
                            helperText = classCommentLabel.errorText,
                            value = classCommentText,
                            error = classCommentLabel.error,
                            rowsMax = 5,
                            rows = 3,
                            variant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    classCommentText = it
                                    classCommentLabel = classCommentLabel.copy(errorText = null)
                                }
                            })
                    }
                }
            }

            umDialogActions {
                umButton(
                    props.systemImpl.getString(MessageID.send, this),
                    endIcon = "send",
                    color = UMColor.secondary,
                    onClick = {
                        if(classCommentText.isNotEmpty()){
                            props.listener?.addComment(classCommentText)
                            setState {
                                classCommentText = ""
                            }
                            handleDialogClosed()
                        }else{
                            setState {
                                classCommentLabel = classCommentLabel.copy(errorText = "")
                            }
                        }
                    }
                )
            }
        }
    }

    private fun handleDialogClosed(){
        props.onDialogClosed.invoke()
        setState {
            showDialog = false
            lastShownAt = props.shownAt
        }
    }
}

fun RBuilder.renderCreateNewComment(
    label: String,
    person: Person? = null,
    listener: DefaultNewCommentItemListener?,
    systemImpl: UstadMobileSystemImpl,
    shownAt: Long = Date().getTime().toLong(),
    onDialogClosed: () -> Unit
) = child(NewCommentsComponent::class) {
    attrs.label = label
    attrs.shownAt = shownAt
    attrs.listener = listener
    attrs.person = person
    attrs.systemImpl = systemImpl
    attrs.onDialogClosed = onDialogClosed
}