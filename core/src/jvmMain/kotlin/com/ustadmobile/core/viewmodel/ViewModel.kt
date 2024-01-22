package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

actual abstract class ViewModel actual constructor(
    savedStateHandle: UstadSavedStateHandle
) {
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Default + Job())

    protected actual open fun onCleared() {

    }

    fun close(){
        onCleared()
        viewModelScope.cancel()
    }

}