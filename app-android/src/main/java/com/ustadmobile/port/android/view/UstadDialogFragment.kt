package com.ustadmobile.port.android.view

import android.content.Context
import androidx.fragment.app.DialogFragment
import com.ustadmobile.core.view.DialogResultListener
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.UstadView
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI

/**
 * Created by mike on 7/17/17.
 */

open class UstadDialogFragment : DialogFragment(), DismissableDialog, UstadView, DIAware {

    override val di by closestDI()

    protected lateinit var mResultListener: DialogResultListener

    override var loading: Boolean = false
        get() = false
        set(value) {
            //TODO: set this on the main activity
            field = value
        }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        (activity as? MainActivity)?.showSnackBar(message, action, actionMessageId)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogResultListener) {
            mResultListener = context
        }
    }

}
