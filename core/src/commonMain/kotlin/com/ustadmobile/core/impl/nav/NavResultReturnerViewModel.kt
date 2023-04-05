package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.viewmodel.ViewModel

/**
 * Wrapper of NavResultReturner as a ViewModel. It can therefor be used via navGraphViewModels
 * on Android
 */
class NavResultReturnerViewModel(
    savedStateHandle: UstadSavedStateHandle,
    private val navResultReturnerImpl: NavResultReturner = NavResultReturnerImpl(),
): ViewModel(savedStateHandle), NavResultReturner by navResultReturnerImpl{


}