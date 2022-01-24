package com.ustadmobile.core.util.ext

import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadViewChoiceDialogListener

actual fun UstadView.showChoiceDialog(context: Any,
                                      title: String?,
                                      items: Array<String>,
                                      listener: UstadViewChoiceDialogListener) {
    //Do nothing - JVM has no GUI
}
