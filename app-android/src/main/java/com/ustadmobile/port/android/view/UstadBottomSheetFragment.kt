package com.ustadmobile.port.android.view

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ustadmobile.core.view.DialogResultListener
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.Runnable
import java.util.*

/**
 * Created by kileha3 on 10/05/20.
 */

open class UstadBottomSheetFragment : BottomSheetDialogFragment(), DismissableDialog, UstadView {

    protected lateinit var mResultListener: DialogResultListener

    private val runOnAttach = Vector<Runnable>()

    override val viewContext: Any
        get() = requireContext()

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        (activity as? MainActivity)?.showSnackBar(message, action, actionMessageId)
    }

    override fun runOnUiThread(r: Runnable?) {
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
