package com.ustadmobile.libuicompose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.view.login.LoginScreen
import com.ustadmobile.libuicompose.view.siteenterlink.SiteEnterLinkScreen
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import org.kodein.di.DI
import kotlin.reflect.KClass

@Composable
fun AppNavHost(
    navigator: Navigator,
    onSetAppUiState: (AppUiState) -> Unit,
    modifier: Modifier,
) {
    val popCommandFlow = remember {
        MutableSharedFlow<PopNavCommand>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    val ustadNavController = remember {
        UstadNavControllerPreCompose(
            navigator = navigator,
            onPopBack = {
                popCommandFlow.tryEmit(it)
            }
        )
    }

    var contentVisible by remember {
        mutableStateOf(true)
    }

    PopNavCommandEffect(
        navigator = navigator,
        popCommandFlow = popCommandFlow,
        onSetContentVisible = { contentVisible = it }
    )

    /*
     * Simple shorthand to pass navController, onSetAppUiState
     */
    @Composable
    fun <T: UstadViewModel> appViewModel(
        backStackEntry: BackStackEntry,
        modelClass: KClass<T>,
        block: (di: DI, UstadSavedStateHandle) -> T
    ) = ustadViewModel(
        modelClass = modelClass,
        backStackEntry = backStackEntry,
        navController = ustadNavController,
        onSetAppUiState = onSetAppUiState,
        block = block
    )

    /*
     * Simple function to show the screen content if content is visible, or hide it otherwise. This
     * is controlled by the PopNavCommandEffect so that content can be hidden before pop navigation
     * starts to avoid rapdily flashing screens.
     */
    fun RouteBuilder.contentScene(
        route: String,
        content: @Composable (BackStackEntry) -> Unit,
    ) {
        scene(route) { backStackEntry ->
            if(contentVisible) {
                content(backStackEntry)
            }
        }
    }

    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = "/${SiteEnterLinkViewModel.DEST_NAME}"
    ) {
        contentScene(
            route = "/${SiteEnterLinkViewModel.DEST_NAME}"
        ) { backStackEntry ->
            SiteEnterLinkScreen(
                viewModel = appViewModel(
                    backStackEntry, SiteEnterLinkViewModel::class
                ) { di, savedStateHandle ->
                    SiteEnterLinkViewModel(di, savedStateHandle)
                }
            )
        }

        contentScene(
            route = "/${LoginViewModel.DEST_NAME}"
        ) { backStackEntry ->
            LoginScreen(
                viewModel = appViewModel(
                    backStackEntry, LoginViewModel::class,
                ) { di, savedStateHandle ->
                    LoginViewModel(di, savedStateHandle)
                }
            )
        }
    }
}