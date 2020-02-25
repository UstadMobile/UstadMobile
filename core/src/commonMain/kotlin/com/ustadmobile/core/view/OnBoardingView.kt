package com.ustadmobile.core.view

interface OnBoardingView : UstadView {

    fun setScreenList()

    fun setLanguageOptions(languages: List<String>)

    fun restartUI()

    companion object {
        const val VIEW_NAME = "OnBoarding"
        const val PREF_TAG = "onboaring_screen"
    }
}
