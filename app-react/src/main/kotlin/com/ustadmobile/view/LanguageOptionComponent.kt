package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.ext.targetChangeValue
import com.ustadmobile.mui.ext.targetInputValue
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.px
import com.ustadmobile.util.*
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.languageComponentLanguageSelectorFormControl
import kotlinx.css.label
import kotlinx.css.width
import org.w3c.dom.events.Event
import react.*
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
            umInputLabel(text, id = "language-label", variant = FormControlVariant.outlined) {
                css(alignTextToStart)
            }

            umSelect(selectedLanguage, native = false,
                labelId = "language-label",
                label = text,
                onChange = { event, _ -> handleOnLanguageChange(event)}) {
                css {
                    width = props.width
                }
                languageOptions.forEach {
                    umMenuItem(it.second, value = it.first){
                        css(alignTextToStart)
                    }
                }
            }
            umFormHelperText(props.caption?:""){
                css(alignTextToStart)
            }
        }
    }

    private fun handleOnLanguageChange(event: Event){
        val value = event.targetChangeValue
        console.log(value)
        props.systemImpl.setLocale(value,this)
        setState { selectedLanguage = value }
        window.location.reload()
    }
}

fun RBuilder.renderLanguages(systemImpl: UstadMobileSystemImpl, width: LinearDimension = 200.px,
                             label : String? = null, caption: String? = null) = child(LanguageOptionComponent::class) {
    attrs.systemImpl = systemImpl
    attrs.width = width
    attrs.label = label
    attrs.caption = caption
}