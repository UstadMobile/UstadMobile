package com.ustadmobile.view

import com.ustadmobile.core.controller.DefaultNewCommentItemListener
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.defaultMarginBottom
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.view.ext.umItem
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.marginTop
import kotlinx.css.padding
import mui.material.FabColor
import mui.material.FabVariant
import mui.material.FormControlVariant
import mui.material.Size
import react.RBuilder
import react.RComponent
import react.setState
import styled.css
import styled.styledSpan

interface CommentProps: UmProps {
    var listener: DefaultNewCommentItemListener?
    var label: String?
}

class NewCommentsComponent(mProps: CommentProps): RComponent<CommentProps, UmState>(mProps) {

    private var commentLabel = FieldLabel(text = props.label)

    private var commentText = ""

    override fun RBuilder.render() {
        umItem(GridSize.cells12, flexDirection = FlexDirection.row){
            css{
                +defaultMarginBottom
            }
            styledSpan {
                css{
                    padding(right = 4.spacingUnits)
                }
                umIcon("person"){
                    css {
                        marginTop = LinearDimension("20px")
                    }
                }
            }

            umItem(if(commentText.isNotEmpty()) GridSize.cells11 else GridSize.cells12,
                if(commentText.isNotEmpty()) GridSize.cells10 else GridSize.cells11){
                umTextFieldMultiLine(
                    value = commentText,
                    label = "${commentLabel.text}",
                    rowsMax = 3,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            commentText = it
                        }
                    }){
                    attrs.onKeyDown = {
                        if(it.shiftKey && it.key.lowercase() == "enter"){
                            it.preventDefault()
                            it.target.asDynamic().value += "\n"
                        }
                        if(!it.shiftKey && it.key.lowercase() == "enter"){
                            it.preventDefault()
                            handleSendComment()
                        }
                    }
                }
            }

            if(commentText.isNotEmpty()){
                umItem(GridSize.cells2, GridSize.cells1, flexDirection = FlexDirection.rowReverse) {
                    umFab("send","",
                        variant = FabVariant.circular,
                        size = Size.large,
                        color = FabColor.secondary,
                        onClick = {
                            stopEventPropagation(it)
                            handleSendComment()
                        }){
                        css{
                            +defaultMarginTop
                        }
                    }
                }
            }

        }
    }

    private fun handleSendComment(){
        if(commentText.isNotEmpty()){
            props.listener?.addComment(commentText)
            setState {
                commentText = ""
            }
        }else {
            setState {
                commentLabel = commentLabel.copy(errorText = "")
            }
        }
    }
}

fun RBuilder.renderCreateNewComment(
    label: String,
    listener: DefaultNewCommentItemListener?
) = child(NewCommentsComponent::class) {
    attrs.label = label
    attrs.listener = listener
}