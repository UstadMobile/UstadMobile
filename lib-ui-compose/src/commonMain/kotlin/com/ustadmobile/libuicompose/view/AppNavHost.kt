package com.ustadmobile.libuicompose.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.view.login.LoginScreen
import com.ustadmobile.libuicompose.view.siteenterlink.SiteEnterLinkScreen
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import org.kodein.di.DI
import kotlin.reflect.KClass

@Composable
fun AppNavHost(
    navigator: Navigator,
    onSetAppUiState: (AppUiState) -> Unit,
    modifier: Modifier,
) {

    val ustadNavController = UstadNavControllerPreCompose(navigator)

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

    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = "/${SiteEnterLinkViewModel.DEST_NAME}"
    ) {
        scene(
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

        scene(
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