package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.nav.NavControllerAdapter
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SNACK_MESSAGE
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.*
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.*
import org.kodein.di.android.x.closestDI

/**
 * Created by mike on 10/15/15.
 */
open class UstadBaseFragment : Fragment(), UstadView, DIAware {

    private var searchView: SearchView? = null

    protected var titleLifecycleObserver: TitleLifecycleObserver? = null

    protected var fabManager: FabManagerLifecycleObserver? = null

    protected var searchManager: SearchViewManagerLifecycleObserver? = null

    protected var progressBarManager: ProgressBarLifecycleObserver? = null

    /**
     * Override and create a child DI to provide access to the multiplatform
     * NavController implementation.
     */
    override val di by DI.lazy {
        val closestDi: DI by closestDI()
        extend(closestDi)

        bind<UstadNavController>() with provider {
            NavControllerAdapter(findNavController(), instance())
        }

        bind<CoroutineScope>(DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
            viewLifecycleOwner.lifecycleScope
        }

        bind<LifecycleOwner>() with provider {
            viewLifecycleOwner
        }
    }

    override var loading: Boolean = false
        get() = field
        set(value) {
            progressBarManager?.visibility = if(value) View.VISIBLE else View.GONE
            field = value
        }


    /**
     * Shortcut to retrieve the SavedState properties from NavController's backstack savedstate
     * handle.
     */
    protected val backStackSavedState: Map<String, String>?
        get() = findNavController().currentBackStackEntrySavedStateMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        searchView = menu.findItem(R.id.menu_search)?.actionView as? SearchView
        searchManager?.searchView = searchView
    }

    /**
     * If enabled, the fab will be managed by this fragment when its view is active.
     */
    protected var fabManagementEnabled: Boolean = true

    var ustadFragmentTitle: String?
        get() = titleLifecycleObserver?.title
        set(value) {
            titleLifecycleObserver?.title = value
        }


    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: StringResource?) {
        (activity as? MainActivity)?.showSnackBar(message, action, actionMessageId)
    }

    companion object {

        const val KEY_ARG_SNACKBAR_SHOWN = "argSnackbarShown"

    }

}
