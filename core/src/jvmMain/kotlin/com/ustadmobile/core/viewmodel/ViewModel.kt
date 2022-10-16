package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.CoroutineScope

actual abstract class ViewModel actual constructor(
    savedStateHandle: UstadSavedStateHandle
) {
    actual val viewModelScope: CoroutineScope
        get() = TODO("Not yet implemented")

    protected actual open fun onCleared() {

    }


}