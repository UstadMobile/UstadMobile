package com.ustadmobile.core.shared

import kotlinx.coroutines.CoroutineScope

expect abstract class OnBoardingModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
}
