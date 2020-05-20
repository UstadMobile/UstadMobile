package com.ustadmobile.port.android.view

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ustadmobile.core.view.DialogResultListener
import com.ustadmobile.core.view.DismissableDialog
import java.util.*

/**
 * Created by kileha3 on 10/05/20.
 */

open class UstadBottomSheetFragment : BottomSheetDialogFragment(), DismissableDialog {

    protected lateinit var mResultListener: DialogResultListener

    private val runOnAttach = Vector<Runnable>()

    fun runOnUiThread(r: Runnable?) {
        if (activity != null) {
            activity?.runOnUiThread(r)
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
