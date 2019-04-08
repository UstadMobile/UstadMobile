package com.ustadmobile.core.view

import java.util.Hashtable

/**
 *
 *
 * Created by mike on 4/16/17.
 */

interface DialogResultListener {

    /**
     *
     * @param commandId
     * @param args
     */
    fun onDialogResult(commandId: Int, dialog: DismissableDialog, args: Hashtable<*, *>)

}
