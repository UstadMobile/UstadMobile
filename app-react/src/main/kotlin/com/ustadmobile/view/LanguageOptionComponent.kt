package com.ustadmobile.view

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.px
import com.ustadmobile.util.*
import org.w3c.dom.events.Event
import react.*

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
        /*mFormControl(variant = MFormControlVariant.outlined) {
            css(languageComponentLanguageSelectorFormControl)

            val text = props.label ?: props.systemImpl.getString(MessageID.language, this)
            mInputLabel(text, htmlFor = "language", variant = MFormControlVariant.outlined) {
                css(alignTextToStart)
            }
            val input = mOutlinedInput(name = "language", id = "language", addAsChild = false, labelWidth = 100)

            mSelect(selectedLanguage, native = false,
                input = input, onChange = { event, _ -> handleOnLanguageChange(event)}) {
                css {
                    width = props.width
                }
                languageOptions.forEach {
                    mMenuItem(it.second, value = it.first){
                        css(alignTextToStart)
                    }
                }
            }
            mFormHelperText(props.caption?:""){
                css(alignTextToStart)
            }
        }*/
    }

    private fun handleOnLanguageChange(event: Event){
        /*val value = event.targetValue.toString()
        props.systemImpl.setLocale(value,this)
        setState { selectedLanguage = value }
        window.location.reload()*/
    }
}

fun RBuilder.renderLanguages(systemImpl: UstadMobileSystemImpl, width: LinearDimension = 200.px,
                             label : String? = null, caption: String? = null) = child(LanguageOptionComponent::class) {
    attrs.systemImpl = systemImpl
    attrs.width = width
    attrs.label = label
    attrs.caption = caption
}