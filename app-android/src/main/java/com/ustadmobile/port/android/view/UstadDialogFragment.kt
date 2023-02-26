package com.ustadmobile.port.android.view

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.DialogResultListener
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.*
import org.kodein.di.android.x.closestDI

/**
 * Created by mike on 7/17/17.
 */

open class UstadDialogFragment : DialogFragment(), DismissableDialog, UstadView, DIAware {

    /**
     * Override and create a child DI to provide access to lifecycle scope
     */
    override val di by DI.lazy {
        val closestDi: DI by closestDI()
        extend(closestDi)

        bind<CoroutineScope>(DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
            lifecycleScope
        }
    }

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
