package com.ustadmobile.view.about

import com.ustadmobile.core.viewmodel.about.OpenLicensesViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadFullSizeIframe
import react.FC
import react.Props


val OpenLicensesScreen = FC<Props> {
    useUstadViewModel { di, savedStateHandle ->
        OpenLicensesViewModel(di, savedStateHandle)
    }

    UstadFullSizeIframe {
        src = "open_source_licenses.txt"
        id = "license_iframe"
    }

}