package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.SiteEnterLinkPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SiteEnterLinkView
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.ext.targetInputValue
import com.ustadmobile.mui.theme.UMColor
import react.RBuilder
import com.ustadmobile.util.*
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.hideOnMobile
import com.ustadmobile.util.ext.clean
import com.ustadmobile.view.ext.createCreateNewItem
import com.ustadmobile.view.ext.createItemWithIconTitleAndDescription
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.height
import kotlinx.css.marginTop
import react.setState
import styled.css
import styled.styledDiv

class SiteEnterLinkComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), SiteEnterLinkView {

    private var mPresenter: SiteEnterLinkPresenter? = null

    override val viewName: String
        get() = SiteEnterLinkView.VIEW_NAME

    var siteLInkLabel = FieldLabel(getString(MessageID.site_link))

    override var siteLink: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var validLink: Boolean = false
        get() = field
        set(value) {
            console.log(value)
            setState {
                field = value
                siteLInkLabel = siteLInkLabel.copy(errorText = if(value) null
                else getString(MessageID.invalid_link))
            }
        }

    override var progressVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private var handlerTimeoutId = -1

    private val inputCheckHandler: () -> Unit = {
        val typedLink = siteLink
        if(typedLink != null){
            progressVisible = true
            mPresenter?.handleCheckLinkText(typedLink)
        }
    }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = SiteEnterLinkPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.render() {
        styledDiv {
            css {
                +contentContainer
                marginTop = 4.spacingUnits
            }
            umGridContainer {
                umItem(GridSize.column3){
                    css(hideOnMobile)
                }
                umItem(GridSize.column12, GridSize.column6){
                    umGridContainer {

                        umItem(GridSize.column12){
                            umTypography(getString(MessageID.please_enter_the_linK),
                                variant = TypographyVariant.body2){
                                css (StyleManager.alignTextToStart)
                            }
                        }

                        umItem(GridSize.column12){
                            umTextField(label = "${siteLInkLabel.text}",
                                helperText = siteLInkLabel.errorText,
                                value = siteLink, error = siteLInkLabel.error,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    window.clearTimeout(handlerTimeoutId)
                                    handlerTimeoutId = window.setTimeout(inputCheckHandler,
                                        INPUT_CHECK_DELAY)

                                    setState {
                                        siteLink = it.targetInputValue
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }

                        if(validLink){
                            umItem(GridSize.column12) {
                                css(defaultMarginTop)
                                umButton(getString(MessageID.next),
                                    size = ButtonSize.large,
                                    color = UMColor.secondary,
                                    variant = ButtonVariant.contained,
                                    onClick = {
                                        mPresenter?.handleClickNext()
                                    }){
                                    css {
                                        +defaultFullWidth
                                        height = LinearDimension("50px")
                                    }}
                            }
                        }

                        umItem(GridSize.column12){
                            umGridContainer(
                                justify = GridJustify.center,
                                alignItems = GridAlignItems.center) {
                                umItem(GridSize.column1){
                                    css(defaultMarginTop)
                                    umTypography(getString(MessageID.or),
                                        variant = TypographyVariant.h6,
                                        align = TypographyAlign.center){
                                        css (StyleManager.alignTextToStart)
                                    }
                                }
                            }
                        }

                        umItem(GridSize.column12){
                            createCreateNewItem(getString(MessageID.create_site))
                        }

                        umItem(GridSize.column12){
                            css(defaultMarginTop)
                            createItemWithIconTitleAndDescription("info",
                                description = getString(MessageID.sites_can_be_help_text).clean(),
                                scaleOnLargeSmall =  true
                            )
                        }
                    }
                }
                umItem(GridSize.column3){
                    css(hideOnMobile)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        siteLink = null
    }


    companion object {
        private const val INPUT_CHECK_DELAY = 500
    }
}