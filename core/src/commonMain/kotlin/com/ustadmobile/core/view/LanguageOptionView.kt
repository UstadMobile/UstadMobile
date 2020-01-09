package com.ustadmobile.core.view

import kotlin.js.JsName

interface LanguageOptionView: UstadView {

    @JsName("restartUI")
    fun restartUI()

    @JsName("showLanguageOptions")
    fun showLanguageOptions()

    @JsName("setCurrentLanguage")
    fun setCurrentLanguage(language: String?)

    @JsName("setLanguageOption")
    fun setLanguageOption(languages: MutableList<String>)

}