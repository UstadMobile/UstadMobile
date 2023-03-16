package com.ustadmobile.port.android.view

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.NavCommandExecutionTracker
import com.ustadmobile.core.impl.nav.NavControllerAdapter
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.port.android.view.util.UstadActivityWithProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * This Fragment is designed to contain Jetpack compose content.
 */
abstract class UstadBaseMvvmFragment: Fragment(), DIAware {

    override val di by closestDI()

    private val navCommandExecTracker: NavCommandExecutionTracker by instance()

    class AppUiStateMenuProvider(
        var appUiState: AppUiState?
    ): MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_done, menu)
            val buttonMenuItem = menu.findItem(R.id.menu_done)
            buttonMenuItem.title = appUiState?.actionBarButtonState?.text ?: ""
            buttonMenuItem.isVisible = appUiState?.actionBarButtonState?.visible ?: false
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if(menuItem.itemId == R.id.menu_done) {
                appUiState?.actionBarButtonState?.onClick?.invoke()
                return true
            }

            return false
        }
    }

    private var mMenuProvider: AppUiStateMenuProvider? = null

    /**
     *
     */
    fun CoroutineScope.launchNavigatorCollector(viewModel: UstadViewModel) {
        launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.navCommandFlow.collect {
                    navCommandExecTracker.runIfNotExecutedOrTimedOut(it) { cmd ->
                        when(cmd) {
                            is NavigateNavCommand -> {
                                //if this is run directly (without launch) then we will get a memory leak
                                launch {
                                    delay(20)
                                    val navAdapter = NavControllerAdapter(findNavController(),
                                        direct.instance())
                                    navAdapter.navigate(cmd.viewName, cmd.args, cmd.goOptions)
                                }
                            }
                            is PopNavCommand -> {
                                launch {
                                    val navAdapter = NavControllerAdapter(findNavController(),
                                        direct.instance())
                                    navAdapter.popBackStack(cmd.viewName, cmd.inclusive)
                                }
                            }
                            else -> {
                                //do nothing
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Collect the AppUiState flow from the given ViewModel, then apply it as required to the AppBar,
     * floating action button, etc.
     */
    fun CoroutineScope.launchAppUiStateCollector(viewModel: UstadViewModel) {
        mMenuProvider = AppUiStateMenuProvider(null).also {
            requireActivity().addMenuProvider(it, viewLifecycleOwner)
        }

        launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.appUiState.collect { appUiState ->
                    (activity as? AppCompatActivity)?.supportActionBar
                        ?.takeIf { it.title != appUiState.title }
                        ?.title = appUiState.title

                    val progressBar = (activity as? UstadActivityWithProgressBar)?.activityProgressBar
                    progressBar?.visibility = if(appUiState.loadingState.loadingState == LoadingUiState.State.INDETERMINATE){
                        View.VISIBLE
                    }else {
                        View.GONE
                    }

                    val fab = (activity as? UstadActivityWithFab)?.activityFloatingActionButton
                    fab.takeIf { it?.text != appUiState.fabState.text }?.text = appUiState.fabState.text

                    if(fab != null && fab.getTag(R.id.tag_fab_uistate_icon) != appUiState.fabState.icon) {
                        fab.icon = if(appUiState.fabState.icon != FabUiState.FabIcon.NONE) {
                            ContextCompat.getDrawable(requireContext(),
                                ICON_MAP[appUiState.fabState.icon] ?: 0)
                        }else{
                            null
                        }

                        fab.setTag(R.id.tag_fab_uistate_icon, appUiState.fabState.icon)
                    }

                    //If this is set by anything else, then the tag could be out of sync with the actual value,
                    // so can't rely on tab until presenter based screens are removed.
                    if(fab != null) {
                        fab.setOnClickListener {
                            appUiState.fabState.onClick()
                        }
                        fab.setTag(R.id.tag_fab_uistate_listener, appUiState.fabState.onClick)
                    }

                    val fabVisibility = if(appUiState.fabState.visible) {
                        View.VISIBLE
                    }else {
                        View.GONE
                    }

                    fab?.takeIf { it.visibility != fabVisibility }?.visibility = fabVisibility


                    val currentActionBarState = mMenuProvider?.appUiState?.actionBarButtonState
                    val newActionBarState = appUiState.actionBarButtonState
                    if(newActionBarState != currentActionBarState) {
                        mMenuProvider?.appUiState = appUiState
                        if(
                            newActionBarState.visible != currentActionBarState?.visible ||
                            newActionBarState.enabled != currentActionBarState?.enabled ||
                            newActionBarState.text != currentActionBarState?.text
                        ) {
                            requireActivity().invalidateMenu()
                        }
                    }
                }
            }
        }
    }

    companion object {

        val ICON_MAP = mapOf(
            FabUiState.FabIcon.EDIT to R.drawable.ic_baseline_edit_24,
            FabUiState.FabIcon.ADD to R.drawable.ic_add_white_24dp,
            FabUiState.FabIcon.NONE to 0,
        )

    }
}