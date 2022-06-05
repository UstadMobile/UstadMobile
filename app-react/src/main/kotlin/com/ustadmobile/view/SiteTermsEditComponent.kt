package com.ustadmobile.view

import com.ustadmobile.core.controller.SiteTermsEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SiteTermsEditView
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.mui.components.FormControlVariant
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umSpacer
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class SiteTermsEditComponent(props: UmProps): UstadEditComponent<SiteTermsWithLanguage>(props),
    SiteTermsEditView {

    private var mPresenter: SiteTermsEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SiteTermsWithLanguage>?
        get() = mPresenter


    override var languageError: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var entity: SiteTermsWithLanguage? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private var languageLabel = FieldLabel(text = getString(MessageID.language))

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.edit_terms_and_policies)
        mPresenter = SiteTermsEditPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +StyleManager.contentContainer
                +StyleManager.defaultPaddingTop
            }

            umItem {
                umTextField(label = "${languageLabel.text}",
                    helperText = languageLabel.errorText,
                    value = entity?.stLanguage?.name,
                    error = languageLabel.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined){
                    attrs.asDynamic().onClick = {
                        mPresenter?.handleClickLanguage()
                    }
                }
            }

            umSpacer()
            umSpacer()

            umItem {
                css(StyleManager.defaultDoubleMarginTop)

                umMuiHtmlEditor(
                    entity?.termsHtml,
                    label = getString(MessageID.type_here),
                    onChange = {
                        entity?.termsHtml = it
                    })
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}