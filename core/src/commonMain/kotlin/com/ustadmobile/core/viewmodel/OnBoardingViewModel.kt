package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI
import org.kodein.di.instance

class OnBoardingViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle) {

    private val _languageList: MutableStateFlow<List<UstadMobileSystemCommon.UiLanguage>>

    val languageList: Flow<List<UstadMobileSystemCommon.UiLanguage>>

    private val _currentLanguage: MutableStateFlow<UstadMobileSystemCommon.UiLanguage>

    val currentLanguage: Flow<UstadMobileSystemCommon.UiLanguage>

    private val systemImpl: UstadMobileSystemImpl by instance()

    init {
        val allLanguages = systemImpl.getAllUiLanguagesList()
        val currentLocaleCode = systemImpl.getLocale()

        _languageList = MutableStateFlow(allLanguages)
        languageList = _languageList.asStateFlow()

        _currentLanguage = MutableStateFlow(allLanguages.first { it.langCode == currentLocaleCode} )
        currentLanguage = _currentLanguage.asStateFlow()
    }

    fun onClickNext(){

    }

    fun onLanguageSelected(uiLanguage: UstadMobileSystemCommon.UiLanguage) {
        //TODO: Set the language
    }

}