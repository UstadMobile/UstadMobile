package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import moe.tlaster.precompose.viewmodel.ViewModel as PreComposeViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope as preComposeViewModelScope
import kotlinx.coroutines.CoroutineScope

actual abstract class ViewModel actual constructor(
    savedStateHandle: UstadSavedStateHandle
): PreComposeViewModel() {

    actual val viewModelScope: CoroutineScope = preComposeViewModelScope

    actual override fun onCleared() {
        super.onCleared()
    }

}