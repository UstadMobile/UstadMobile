package com.ustadmobile.view

import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.form.mFormHelperText
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.mOutlinedInput
import com.ccfraser.muirwik.components.mSelect
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ccfraser.muirwik.components.targetValue
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.languageComponentLanguageSelectorFormControl
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.px
import kotlinx.css.width
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css

interface LanguageProps: RProps {
    var systemImpl: UstadMobileSystemImpl
    var width: LinearDimension
    var caption: String?
    var label: String?
}


class  LanguageComponent(mProps: LanguageProps): UstadBaseComponent<LanguageProps,RState>(mProps){

    private lateinit var languageOptions: List<Pair<String, String>>

    private lateinit var selectedLanguage: Any

    override fun RState.init(props: LanguageProps) {
        languageOptions = props.systemImpl.getAllUiLanguagesList(this)
        val selectedLocaleIndex = languageOptions.indexOfFirst {
            it.first == props.systemImpl.getDisplayedLocale(this) }
        selectedLanguage = languageOptions[selectedLocaleIndex].first
    }

    override fun RBuilder.render() {
        mFormControl(variant = MFormControlVariant.outlined) {
            css(languageComponentLanguageSelectorFormControl)
            mInputLabel(props.label?:getString(MessageID.language), htmlFor = "language", variant = MFormControlVariant.outlined) {
                css(alignTextToStart)
            }
            mSelect(selectedLanguage, native = false,
                input = mOutlinedInput(name = "language", id = "language", addAsChild = false,
                    labelWidth = 100), onChange = { event, _ -> handleOnLanguageChange(event)}) {
                css { width = props.width}
                languageOptions.forEach {
                    mMenuItem(it.second, value = it.first){
                        css(alignTextToStart)
                    }
                }
            }
            mFormHelperText(props.caption?:""){
                css(alignTextToStart)
            }
        }
    }

    private fun handleOnLanguageChange(event: Event){
        val value = event.targetValue.toString()
        props.systemImpl.setLocale(value,this)
        setState { selectedLanguage = value }
        window.location.reload()
    }

    override val viewName: String?
        get() = null

    override fun onComponentReady() {}
}

fun RBuilder.renderLanguages(systemImpl: UstadMobileSystemImpl, width: LinearDimension = 200.px,
                             label : String? = null, caption: String? = null) = child(LanguageComponent::class) {
    attrs.systemImpl = systemImpl
    attrs.width = width
    attrs.label = label
    attrs.caption = caption
}