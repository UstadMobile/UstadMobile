package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel(
    savedStateHandle: UstadSavedStateHandle
) {

    val viewModelScope: CoroutineScope

    protected open fun onCleared()

}