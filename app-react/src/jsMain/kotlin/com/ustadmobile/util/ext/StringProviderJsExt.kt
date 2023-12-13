package com.ustadmobile.util.ext

import com.ustadmobile.core.impl.locale.StringProviderJs
import com.ustadmobile.core.MR

fun StringProviderJs.yesOrNoString(yes: Boolean?): String {
    return this[ if(yes == true) MR.strings.yes else MR.strings.no ]
}
