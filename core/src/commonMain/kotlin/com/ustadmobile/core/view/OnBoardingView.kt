package com.ustadmobile.core.view

interface OnBoardingView : UstadView {

    fun setScreenList()

    fun setLanguageOptions(languages: MutableList<String>)

    fun restartUI()

    companion object {
        const val VIEW_NAME = "OnBoarding"
        const val PREF_TAG = "onboaring_screen"
    }
}
