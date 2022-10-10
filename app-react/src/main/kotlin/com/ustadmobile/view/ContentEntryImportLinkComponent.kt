package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.marginTop
import mui.material.FormControlVariant
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ContentEntryImportLinkComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps),
    ContentEntryImportLinkView {

    private var mPresenter: ContentEntryImportLinkPresenter? = null


    var importLinkLabel = FieldLabel(getString(MessageID.enter_url))

    var importLink: String = ""

    override var inProgress: Boolean = false
        get() = field
        set(value) {
            loading = value
            setState {
                field = value
            }
        }

    override var validLink: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
                importLinkLabel = importLinkLabel.copy(errorText = if(value) null
                else getString(MessageID.invalid_link))
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.enter_url)
        fabManager?.visible = true
        fabManager?.icon = "done"
        fabManager?.text = getString(MessageID.next)
        fabManager?.onClickListener = {
            mPresenter?.handleClickDone(importLink)
        }
        mPresenter = ContentEntryImportLinkPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.render() {
        styledDiv {
            css {
                +contentContainer
                marginTop = 4.spacingUnits
            }

            umGridContainer(GridSpacing.spacing4) {

                umItem(GridSize.cells12){
                    umTextField(
                        label = "${importLinkLabel.text}",
                        helperText = importLinkLabel.errorText,
                        value = importLink,
                        error = importLinkLabel.error,
                        disabled = inProgress,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                importLink = it
                            }
                        })
                }

                umItem(GridSize.cells12){
                    css(defaultMarginTop)
                    umTypography(getString(MessageID.supported_link),
                        variant = TypographyVariant.body1,
                        align = TypographyAlign.center){
                        css (StyleManager.alignTextToStart)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}