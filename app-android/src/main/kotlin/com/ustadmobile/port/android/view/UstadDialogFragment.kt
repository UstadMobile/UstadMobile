package com.ustadmobile.port.android.view

import android.content.Context
import androidx.fragment.app.DialogFragment
import com.ustadmobile.core.view.DialogResultListener
import com.ustadmobile.core.view.DismissableDialog
import java.util.*

/**
 * Created by mike on 7/17/17.
 */

open class UstadDialogFragment : DialogFragment(), DismissableDialog {

    protected lateinit var mResultListener: DialogResultListener

    private val runOnAttach = Vector<Runnable>()

    fun runOnUiThread(r: Runnable?) {
        if (activity != null) {
            activity!!.runOnUiThread(r)
        } else {
            runOnAttach.add(r)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogResultListener) {
            mResultListener = context
        }

        val runnables = runOnAttach.iterator()
        while (runnables.hasNext()) {
            val current = runnables.next()
            current.run()
            runnables.remove()
        }
    }

}
