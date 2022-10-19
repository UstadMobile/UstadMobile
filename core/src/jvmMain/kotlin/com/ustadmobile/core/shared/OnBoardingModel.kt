package com.ustadmobile.core.shared

import kotlinx.coroutines.CoroutineScope

actual abstract class OnBoardingModel actual constructor() {
    actual val viewModelScope: CoroutineScope
        get() = TODO("Not yet implemented")

    protected actual open fun onCleared() {
    }
}