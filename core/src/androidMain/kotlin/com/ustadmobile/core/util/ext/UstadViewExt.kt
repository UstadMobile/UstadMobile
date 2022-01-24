package com.ustadmobile.core.util.ext

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadViewChoiceDialogListener

actual fun UstadView.showChoiceDialog(context: Any,
                                      title: String?,
                                      items: Array<String>,
                                      listener: UstadViewChoiceDialogListener ) {

    MaterialAlertDialogBuilder(context as Context)
        .apply {
            if(title != null)
                setTitle(title)
        }
        .setItems(items) { dialog, which ->
            listener.onChoiceSelected(which)
        }
        .show()

}
