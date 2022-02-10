package com.ustadmobile.view

import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.SiteEnterLinkView
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.marginTop
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ContentEntryImportLinkComponent(mProps: UmProps): UstadEditComponent<String>(mProps),
    ContentEntryImportLinkView {

    private var mPresenter: ContentEntryImportLinkPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, String>?
        get() = mPresenter

    override val viewNames: List<String>
        get() = listOf(SiteEnterLinkView.VIEW_NAME)

    var importLinkLabel = FieldLabel(getString(MessageID.enter_url))

    override var showProgress: Boolean = false
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

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.enter_url)
        mPresenter = ContentEntryImportLinkPresenter(this, arguments, this, this, di)
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
                    umTextField(label = "${importLinkLabel.text}",
                        helperText = importLinkLabel.errorText,
                        value = entity, error = importLinkLabel.error,
                        disabled = showProgress,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity = it
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
        entity = null
    }
}