package com.ustadmobile.core.domain.hidekeyboard

/**
 * Sometimes a ViewModel needs to hide the soft keyboard (on mobile). This is implemented in Jetpack
 * Compose alongside the ustadViewModel function to provide an implementation to ViewModels.
 */
fun interface HideKeyboardUseCase {

    operator fun invoke()

}