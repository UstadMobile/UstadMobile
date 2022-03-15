package com.ustadmobile.core.view

interface OnBoardingView : UstadView {

    fun setLanguageOptions(languages: List<String>, currentSelection: String)

    fun restartUI()

    companion object {
        const val VIEW_NAME = "OnBoardingView"
        const val PREF_TAG = "onboaring_screen"
    }
}
