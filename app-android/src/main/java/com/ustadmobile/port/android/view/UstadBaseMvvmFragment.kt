package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.NavCommandExecutionTracker
import com.ustadmobile.core.impl.nav.NavControllerAdapter
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

abstract class UstadBaseMvvmFragment: UstadBaseFragment() {

    override val di by DI.lazy {
        val closestDi: DI by closestDI()
        extend(closestDi)
    }

    val navCommandExecTracker: NavCommandExecutionTracker by instance()

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
     *
     */
    fun CoroutineScope.launchAppUiStateCollector(viewModel: UstadViewModel) {
        launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.appUiState.collect {
                    ustadFragmentTitle = it.title
                    loading = it.loadingState.loadingState == LoadingUiState.State.INDETERMINATE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}