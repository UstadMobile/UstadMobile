package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.SiteEnterLinkPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SiteEnterLinkView
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.ext.clean
import com.ustadmobile.view.ext.createCreateNewItem
import com.ustadmobile.view.ext.createItemWithIconTitleAndDescription
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.window
import kotlinx.css.height
import kotlinx.css.marginTop
import kotlinx.css.px
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

class SiteEnterLinkComponent(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), SiteEnterLinkView {

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

    override fun onCreate() {
        super.onCreate()
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
                mHidden(smDown = true){
                    umItem(MGridSize.cells3){}
                }
                umItem(MGridSize.cells12, MGridSize.cells6){
                    umGridContainer {

                        umItem(MGridSize.cells12){
                            mTypography(getString(MessageID.please_enter_the_linK),
                                variant = MTypographyVariant.body2,
                                color = MTypographyColor.textPrimary){
                                css (StyleManager.alignTextToStart)
                            }
                        }

                        umItem(MGridSize.cells12){
                            mTextField(label = "${siteLInkLabel.text}",
                                helperText = siteLInkLabel.errorText,
                                value = siteLink, error = siteLInkLabel.error,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
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
                            umItem(MGridSize.cells12) {
                                css(defaultMarginTop)
                                mButton(getString(MessageID.next),
                                    size = MButtonSize.large,
                                    color = MColor.secondary,
                                    variant = MButtonVariant.contained,
                                    onClick = {
                                        mPresenter?.handleClickNext()
                                    }){
                                    css {
                                        +defaultFullWidth
                                        height = 50.px
                                    }}
                            }
                        }

                        umItem(MGridSize.cells12){
                            umGridContainer(
                                justify = MGridJustify.center,
                                alignItems = MGridAlignItems.center) {
                                umItem(MGridSize.cells1){
                                    css(defaultMarginTop)
                                    mTypography(getString(MessageID.or),
                                        variant = MTypographyVariant.h6,
                                        align = MTypographyAlign.center,
                                        color = MTypographyColor.textPrimary){
                                        css (StyleManager.alignTextToStart)
                                    }
                                }
                            }
                        }

                        umItem(MGridSize.cells12){
                            createCreateNewItem(getString(MessageID.create_site))
                        }

                        umItem(MGridSize.cells12){
                            css(defaultMarginTop)
                            createItemWithIconTitleAndDescription("info",
                                description = getString(MessageID.sites_can_be_help_text).clean(),
                                scaleOnLargeSmall =  true
                            )
                        }
                    }
                }
                mHidden(smDown = true){
                    umItem(MGridSize.cells3){}
                }
            }
        }
    }


    companion object {
        private const val INPUT_CHECK_DELAY = 500
    }
}