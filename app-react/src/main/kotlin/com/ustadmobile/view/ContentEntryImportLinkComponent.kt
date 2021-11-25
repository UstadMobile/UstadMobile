package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.view.SiteEnterLinkView
import react.RBuilder
import com.ustadmobile.util.*
import react.setState

class ContentEntryImportLinkComponent(mProps: UmProps): UstadEditComponent<String>(mProps),
    ContentEntryImportLinkView {

    private var mPresenter: ContentEntryImportLinkPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, String>?
        get() = mPresenter

    override val viewName: String
        get() = SiteEnterLinkView.VIEW_NAME

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
        title = getString(MessageID.enter_url)
        mPresenter = ContentEntryImportLinkPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.render() {
       /* styledDiv {
            css {
                +contentContainer
                marginTop = 4.spacingUnits
            }

            umItem(MGridSize.cells12){
                mTextField(label = "${importLinkLabel.text}",
                    helperText = importLinkLabel.errorText,
                    value = entity, error = importLinkLabel.error,
                    disabled = showProgress,
                    variant = MFormControlVariant.outlined,
                    onChange = {
                        it.persist()
                        setState {
                            entity = it.targetInputValue
                        }
                    }){
                    css(defaultFullWidth)
                }
            }

            umItem(MGridSize.cells12){
                css(defaultMarginTop)
                mTypography(getString(MessageID.supported_link),
                    variant = MTypographyVariant.body1,
                    align = MTypographyAlign.center,
                    color = MTypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }
}