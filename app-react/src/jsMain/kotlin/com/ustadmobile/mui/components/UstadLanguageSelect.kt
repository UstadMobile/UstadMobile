package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import mui.material.FormControl
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Select
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useRequiredContext
import web.cssom.Color

external interface UstadLanguageSelectProps: Props {

    var langList: List<UstadMobileSystemCommon.UiLanguage>

    var currentLanguage: UstadMobileSystemCommon.UiLanguage

    var onItemSelected: (UstadMobileSystemCommon.UiLanguage) -> Unit

    var fullWidth: Boolean?

    var label: ReactNode

    var id: String

    var disabled: Boolean?
}

val UstadLanguageSelect = FC<UstadLanguageSelectProps> { props ->
    /*
     * The language setting for "use system language" is a blank string. This doesn't work with a
     * select field value. So we will convert a blank string to sys and back to blank
     */
    fun String.toLangSysVal() = if(this == SupportedLanguagesConfig.LOCALE_USE_SYSTEM) "sys" else this

    fun String.fromLangSysVal() = if(this == "sys")
        SupportedLanguagesConfig.LOCALE_USE_SYSTEM
    else
        this

    val theme by useRequiredContext(ThemeContext)


    FormControl {
        fullWidth = props.fullWidth ?: true

        InputLabel {
            sx {
                backgroundColor = Color(theme.palette.background.default)
            }

            id = "${props.id}_label"
            +props.label
        }

        Select {
            value = props.currentLanguage.langCode.toLangSysVal()
            id = props.id
            label = label
            labelId = "${props.id}_label"
            disabled = props.disabled ?: false
            fullWidth = props.fullWidth ?: true
            onChange = { event, _ ->
                val selectedVal = ("" + event.target.value).fromLangSysVal()
                val selectedLang = props.langList.first {
                    it.langCode == selectedVal
                }
                props.onItemSelected(selectedLang)
            }

            props.langList.forEach { lang ->
                MenuItem {
                    value = lang.langCode.toLangSysVal()
                    + lang.langDisplay
                }
            }
        }
    }
}