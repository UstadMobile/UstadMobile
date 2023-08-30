package com.ustadmobile.view.redirect

import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import com.ustadmobile.hooks.useUstadViewModel
import react.FC
import react.Props

val RedirectScreen = FC<Props> {
    useUstadViewModel { di, savedStateHandle ->
        RedirectViewModel(di, savedStateHandle)
    }
    //There is no UI for Redirect
}
