package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.FormControlVariant
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umFormControl
import com.ustadmobile.mui.components.umTextFieldSelect
import com.ustadmobile.util.StyleManager.languageComponentLanguageSelectorFormControl
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import react.RBuilder
import react.RComponent
import react.setState
import styled.css

interface LanguageProps: UmProps {
    var systemImpl: UstadMobileSystemImpl
    var width: LinearDimension
    var caption: String?
    var label: String?
}


class  LanguageOptionComponent(mProps: LanguageProps): RComponent<LanguageProps,UmState>(mProps){

    private lateinit var languageOptions: List<Pair<String, String>>

    private lateinit var selectedLanguage: Any

    override fun UmState.init(props: LanguageProps) {
        languageOptions = props.systemImpl.getAllUiLanguagesList(this)
        val selectedLocaleIndex = languageOptions.indexOfFirst {
            it.first == props.systemImpl.getDisplayedLocale(this) }
        selectedLanguage = languageOptions[selectedLocaleIndex].first
    }

    override fun RBuilder.render() {
        umFormControl(variant = FormControlVariant.outlined) {
            css(languageComponentLanguageSelectorFormControl)
            val text = props.label ?: props.systemImpl.getString(MessageID.language, this)
            umTextFieldSelect(text,
                props.systemImpl.getDisplayedLocale(this),
                props.caption?:"",
                values = languageOptions,
                variant = FormControlVariant.outlined,
                onChange = {
                    handleOnLanguageChange(it)
                }
            )
        }
    }

    private fun handleOnLanguageChange(value: String){
        props.systemImpl.setLocale(value,this)
        setState { selectedLanguage = value }
        window.location.reload()
    }
}

fun RBuilder.renderLanguages(
    systemImpl: UstadMobileSystemImpl,
    width: LinearDimension = 40.spacingUnits,
    label : String? = null,
    caption: String? = null
) = child(LanguageOptionComponent::class) {
    attrs.systemImpl = systemImpl
    attrs.width = width
    attrs.label = label
    attrs.caption = caption
}