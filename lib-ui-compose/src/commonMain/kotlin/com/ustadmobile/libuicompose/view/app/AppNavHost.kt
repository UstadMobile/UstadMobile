package com.ustadmobile.libuicompose.view.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.NavResultReturnerImpl
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.util.PopNavCommandEffect
import com.ustadmobile.libuicompose.view.clazz.detail.ClazzDetailScreen
import com.ustadmobile.libuicompose.view.clazz.edit.ClazzEditScreen
import com.ustadmobile.libuicompose.view.clazz.list.ClazzListScreen
import com.ustadmobile.libuicompose.view.clazzassignment.courseblockedit.CourseBlockEditScreen
import com.ustadmobile.libuicompose.view.clazzenrolment.edit.ClazzEnrolmentEditScreen
import com.ustadmobile.libuicompose.view.clazzenrolment.list.ClazzEnrolmentListScreen
import com.ustadmobile.libuicompose.view.contententry.list.ContentEntryListScreenForViewModel
import com.ustadmobile.libuicompose.view.login.LoginScreen
import com.ustadmobile.libuicompose.view.person.accountedit.PersonAccountEditScreen
import com.ustadmobile.libuicompose.view.person.detail.PersonDetailScreen
import com.ustadmobile.libuicompose.view.person.edit.PersonEditScreen
import com.ustadmobile.libuicompose.view.person.list.PersonListScreen
import com.ustadmobile.libuicompose.view.schedule.edit.ScheduleEditScreen
import com.ustadmobile.libuicompose.view.siteenterlink.SiteEnterLinkScreen
import com.ustadmobile.libuicompose.view.timezone.TimeZoneListScreen
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
    persistNavState: Boolean = false,
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

    val navResultReturner: NavResultReturner = remember {
        NavResultReturnerImpl()
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
        navResultReturner = navResultReturner,
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
        initialRoute = "/${RedirectViewModel.DEST_NAME}",
        persistNavState = persistNavState,
    ) {

        contentScene("/${RedirectViewModel.DEST_NAME}") { backStackEntry ->
            //No UI for redirect
            appViewModel(backStackEntry, RedirectViewModel::class) { di, savedStateHandle ->
                RedirectViewModel(di, savedStateHandle)
            }
        }

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

        ContentEntryListViewModel.ALL_DEST_NAMES.forEach { destName ->
            contentScene("/$destName") { backStackEntry ->
                ContentEntryListScreenForViewModel(
                    viewModel = appViewModel(
                        backStackEntry, ContentEntryListViewModel::class,
                    ) { di, savedStateHandle ->
                        ContentEntryListViewModel(di, savedStateHandle, destName)
                    }
                )
            }
        }

        ClazzListViewModel.ALL_DEST_NAMES.forEach { destName ->
            contentScene("/$destName") { backStackEntry ->
                ClazzListScreen(
                    backStackEntry, ustadNavController, onSetAppUiState, navResultReturner, destName
                )
            }
        }

        contentScene("/${ClazzEditViewModel.DEST_NAME}") {backStackEntry ->
            ClazzEditScreen(
                appViewModel(
                    backStackEntry, ClazzEditViewModel::class
                ) { di, savedStateHandle ->
                    ClazzEditViewModel(di, savedStateHandle)
                }
            )
        }

        contentScene("/${ScheduleEditViewModel.DEST_NAME}") { backStackEntry ->
            ScheduleEditScreen(
                appViewModel(
                    backStackEntry, ScheduleEditViewModel::class
                ) { di, savedStateHandle ->
                    ScheduleEditViewModel(di, savedStateHandle)
                }
            )
        }

        contentScene("/${ClazzDetailViewModel.DEST_NAME}") { backStackEntry ->
            ClazzDetailScreen(
                backStackEntry, ustadNavController, onSetAppUiState, navResultReturner,
            )
        }

        contentScene("/${CourseBlockEditViewModel.DEST_NAME}") { backStackEntry ->
            CourseBlockEditScreen(
                appViewModel(
                    backStackEntry, CourseBlockEditViewModel::class
                ) { di, savedStateHandle ->
                    CourseBlockEditViewModel(di, savedStateHandle)
                }
            )
        }

        contentScene("/${TimeZoneListViewModel.DEST_NAME}") {backStackEntry ->
            TimeZoneListScreen(
                appViewModel(
                    backStackEntry, TimeZoneListViewModel::class
                ) { di, savedStateHandle ->
                    TimeZoneListViewModel(di, savedStateHandle)
                }
            )
        }

        PersonListViewModel.ALL_DEST_NAMES.forEach { destName ->
            contentScene("/$destName") { backStackEntry ->
                PersonListScreen(
                    appViewModel(
                        backStackEntry, PersonListViewModel::class
                    ) { di, savedStateHandle ->
                        PersonListViewModel(di, savedStateHandle, destName)
                    }
                )
            }
        }

        contentScene("/${ClazzEnrolmentEditViewModel.DEST_NAME}") { backStackEntry ->
            ClazzEnrolmentEditScreen(
                appViewModel(
                    backStackEntry, ClazzEnrolmentEditViewModel::class,
                ) { di, savedStateHandle ->
                    ClazzEnrolmentEditViewModel(di, savedStateHandle)
                }
            )
        }

        contentScene("/${ClazzEnrolmentListViewModel.DEST_NAME}") { backStackEntry ->
            ClazzEnrolmentListScreen(
                appViewModel(
                    backStackEntry, ClazzEnrolmentListViewModel::class, ::ClazzEnrolmentListViewModel
                )
            )
        }

        contentScene("/${PersonDetailViewModel.DEST_NAME}") { backStackEntry ->
            PersonDetailScreen(
                appViewModel(
                    backStackEntry, PersonDetailViewModel::class, ::PersonDetailViewModel
                )
            )
        }

        contentScene("/${PersonEditViewModel.DEST_NAME}") { backStackEntry ->
            PersonEditScreen(
                appViewModel(
                    backStackEntry, PersonEditViewModel::class, ::PersonEditViewModel
                )
            )
        }

        contentScene("/${PersonAccountEditViewModel.DEST_NAME}") { backStackEntry ->
            PersonAccountEditScreen(
                appViewModel(
                    backStackEntry, PersonAccountEditViewModel::class, ::PersonAccountEditViewModel
                )
            )
        }


    }


}