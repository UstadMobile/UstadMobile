package com.ustadmobile.libuicompose.view.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.NavResultReturnerImpl
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailViewModel
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.submissiondetail.CourseAssignmentSubmissionDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.textblockdetail.TextBlockDetailViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditViewModel
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListViewModel
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditViewModel
import com.ustadmobile.core.viewmodel.settings.SettingsViewModel
import com.ustadmobile.core.viewmodel.site.detail.SiteDetailViewModel
import com.ustadmobile.core.viewmodel.site.edit.SiteEditViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.util.NavControllerUriHandler
import com.ustadmobile.libuicompose.util.PopNavCommandEffect
import com.ustadmobile.libuicompose.view.accountlist.AccountListScreen
import com.ustadmobile.libuicompose.view.clazz.detail.ClazzDetailScreen
import com.ustadmobile.libuicompose.view.clazz.edit.ClazzEditScreen
import com.ustadmobile.libuicompose.view.clazz.list.ClazzListScreen
import com.ustadmobile.libuicompose.view.clazzassignment.courseblockedit.CourseBlockEditScreen
import com.ustadmobile.libuicompose.view.clazzassignment.detail.ClazzAssignmentDetailScreen
import com.ustadmobile.libuicompose.view.clazzassignment.edit.ClazzAssignmentEditScreen
import com.ustadmobile.libuicompose.view.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditScreen
import com.ustadmobile.libuicompose.view.clazzassignment.submissiondetail.CourseAssignmentSubmissionDetailScreen
import com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailScreen
import com.ustadmobile.libuicompose.view.clazzenrolment.edit.ClazzEnrolmentEditScreen
import com.ustadmobile.libuicompose.view.clazzenrolment.list.ClazzEnrolmentListScreen
import com.ustadmobile.libuicompose.view.clazzlog.attendancelist.ClazzLogListAttendanceScreen
import com.ustadmobile.libuicompose.view.clazzlog.edit.ClazzLogEditScreen
import com.ustadmobile.libuicompose.view.clazzlog.editattendance.ClazzLogEditAttendanceScreen
import com.ustadmobile.libuicompose.view.contententry.list.ContentEntryListScreenForViewModel
import com.ustadmobile.libuicompose.view.courseblock.textblockdetail.TextBlockDetailScreen
import com.ustadmobile.libuicompose.view.coursegroupset.detail.CourseGroupSetDetailScreen
import com.ustadmobile.libuicompose.view.coursegroupset.edit.CourseGroupSetEditScreen
import com.ustadmobile.libuicompose.view.coursegroupset.list.CourseGroupSetListScreen
import com.ustadmobile.libuicompose.view.discussionpost.coursediscussiondetail.CourseDiscussionDetailScreen
import com.ustadmobile.libuicompose.view.discussionpost.detail.DiscussionPostDetailScreen
import com.ustadmobile.libuicompose.view.discussionpost.edit.DiscussionPostEditScreen
import com.ustadmobile.libuicompose.view.htmledit.HtmlEditScreen
import com.ustadmobile.libuicompose.view.login.LoginScreen
import com.ustadmobile.libuicompose.view.onboarding.OnboardingScreen
import com.ustadmobile.libuicompose.view.person.accountedit.PersonAccountEditScreen
import com.ustadmobile.libuicompose.view.person.detail.PersonDetailScreen
import com.ustadmobile.libuicompose.view.person.edit.PersonEditScreen
import com.ustadmobile.libuicompose.view.person.list.PersonListScreen
import com.ustadmobile.libuicompose.view.person.registerageredirect.RegisterAgeRedirectScreen
import com.ustadmobile.libuicompose.view.schedule.edit.ScheduleEditScreen
import com.ustadmobile.libuicompose.view.settings.SettingsScreen
import com.ustadmobile.libuicompose.view.site.detail.SiteDetailScreen
import com.ustadmobile.libuicompose.view.site.edit.SiteEditScreen
import com.ustadmobile.libuicompose.view.site.termsdetail.SiteTermsDetailScreen
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
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.reflect.KClass
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel
import com.ustadmobile.libuicompose.view.contententry.edit.ContentEntryEditScreen
import com.ustadmobile.libuicompose.view.contententry.getmetadata.ContentEntryGetMetadataScreen
import com.ustadmobile.libuicompose.view.courseterminology.edit.CourseTerminologyEditScreen
import com.ustadmobile.libuicompose.view.courseterminology.list.CourseTerminologyListScreen
import com.ustadmobile.libuicompose.view.parentalconsentmanagement.ParentalConsentManagementScreen
import com.ustadmobile.libuicompose.view.person.registerminorwaitforparent.RegisterMinorWaitForParentScreen
import kotlinx.coroutines.flow.Flow

/**
 * @param navCommandFlow A (hoisted) flow of navigation commands. This can be used by the underlying
 *                       platform (e.g. Android/Desktop) to emit navigation commands when a command
 *                       is received (e.g. by onNewIntent).
 */
@Composable
fun AppNavHost(
    navigator: Navigator,
    onSetAppUiState: (AppUiState) -> Unit,
    onShowSnackBar: SnackBarDispatcher,
    persistNavState: Boolean = false,
    modifier: Modifier,
    navCommandFlow: Flow<NavCommand>? = null,
    initialRoute: String = "/${RedirectViewModel.DEST_NAME}",
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

    LaunchedEffect(navCommandFlow) {
        navCommandFlow?.collect {
            ustadNavController.onCollectNavCommand(it)
        }
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
        onShowSnackBar = onShowSnackBar,
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

    val di = localDI()

    val navControllerUriHandler = remember {
        NavControllerUriHandler(
            navController = ustadNavController,
            accountManager = di.direct.instance(),
            openExternalLinkUseCase = di.direct.instance(),
            apiUrlConfig = di.direct.instance()
        )
    }

    CompositionLocalProvider(LocalUriHandler provides navControllerUriHandler) {
        NavHost(
            modifier = modifier,
            navigator = navigator,
            initialRoute = initialRoute,
            persistNavState = persistNavState,
        ) {

            contentScene("/${RedirectViewModel.DEST_NAME}") { backStackEntry ->
                //No UI for redirect
                appViewModel(backStackEntry, RedirectViewModel::class) { di, savedStateHandle ->
                    RedirectViewModel(di, savedStateHandle)
                }
            }

            contentScene("/${OnBoardingViewModel.DEST_NAME}") { backStackEntry ->
                OnboardingScreen(
                    appViewModel(backStackEntry, OnBoardingViewModel::class, ::OnBoardingViewModel)
                )
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
                        backStackEntry, ustadNavController, onSetAppUiState, navResultReturner, onShowSnackBar, destName
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
                    backStackEntry, ustadNavController, onSetAppUiState, onShowSnackBar, navResultReturner,
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

            PersonEditViewModel.ALL_DEST_NAMES.forEach { destName ->
                contentScene("/$destName") { backStackEntry ->
                    PersonEditScreen(
                        appViewModel(
                            backStackEntry, PersonEditViewModel::class
                        ) { di, savedStateHandle ->
                            PersonEditViewModel(di, savedStateHandle, destName)
                        }
                    )
                }
            }

            contentScene("/${PersonAccountEditViewModel.DEST_NAME}") { backStackEntry ->
                PersonAccountEditScreen(
                    appViewModel(
                        backStackEntry, PersonAccountEditViewModel::class, ::PersonAccountEditViewModel
                    )
                )
            }

            contentScene("/${AccountListViewModel.DEST_NAME}") { backStackEntry ->
                AccountListScreen(
                    appViewModel(backStackEntry, AccountListViewModel::class, ::AccountListViewModel)
                )
            }

            contentScene("/${CourseDiscussionDetailViewModel.DEST_NAME}") { backStackEntry ->
                CourseDiscussionDetailScreen(
                    appViewModel(backStackEntry, CourseDiscussionDetailViewModel::class,
                        ::CourseDiscussionDetailViewModel)
                )
            }

            contentScene("/${DiscussionPostDetailViewModel.DEST_NAME}") { backStackEntry ->
                DiscussionPostDetailScreen(
                    appViewModel(backStackEntry, DiscussionPostDetailViewModel::class,
                        ::DiscussionPostDetailViewModel)
                )
            }

            contentScene("/${DiscussionPostEditViewModel.DEST_NAME}") { backStackEntry ->
                DiscussionPostEditScreen(
                    appViewModel(backStackEntry, DiscussionPostEditViewModel::class,
                        ::DiscussionPostEditViewModel)
                )
            }

            contentScene("/${HtmlEditViewModel.DEST_NAME}") { backStackEntry ->
                HtmlEditScreen(
                    appViewModel(backStackEntry, HtmlEditViewModel::class, ::HtmlEditViewModel)
                )
            }

            contentScene("/${ClazzAssignmentEditViewModel.DEST_NAME}") { backStackEntry ->
                ClazzAssignmentEditScreen(
                    appViewModel(backStackEntry, ClazzAssignmentEditViewModel::class,
                        ::ClazzAssignmentEditViewModel)
                )
            }

            contentScene("/${PeerReviewerAllocationEditViewModel.DEST_NAME}") { backStackEntry ->
                PeerReviewerAllocationEditScreen(
                    appViewModel(backStackEntry, PeerReviewerAllocationEditViewModel::class,
                        ::PeerReviewerAllocationEditViewModel)
                )
            }

            contentScene("/${ClazzAssignmentDetailViewModel.DEST_NAME}") { backStackEntry ->
                ClazzAssignmentDetailScreen(
                    backStackEntry, ustadNavController, onSetAppUiState, onShowSnackBar, navResultReturner,
                )
            }

            contentScene("/${CourseGroupSetDetailViewModel.DEST_NAME}") { backStackEntry ->
                CourseGroupSetDetailScreen(
                    appViewModel(backStackEntry, CourseGroupSetDetailViewModel::class,
                        ::CourseGroupSetDetailViewModel)
                )
            }

            contentScene("/${CourseGroupSetEditViewModel.DEST_NAME}") { backStackEntry ->
                CourseGroupSetEditScreen(
                    appViewModel(backStackEntry, CourseGroupSetEditViewModel::class,
                        ::CourseGroupSetEditViewModel)
                )
            }
            contentScene("/${TextBlockDetailViewModel.DEST_NAME}") { backStackEntry ->
                TextBlockDetailScreen(
                    appViewModel(
                        backStackEntry, TextBlockDetailViewModel::class, ::TextBlockDetailViewModel
                    )
                )
            }
            contentScene("/${ClazzAssignmentSubmitterDetailViewModel.DEST_NAME}") { backStackEntry ->
                ClazzAssignmentSubmitterDetailScreen(
                    appViewModel(backStackEntry, ClazzAssignmentSubmitterDetailViewModel::class,
                        ::ClazzAssignmentSubmitterDetailViewModel)
                )
            }

            contentScene("/${CourseAssignmentSubmissionDetailViewModel.DEST_NAME}") { backStackEntry ->
                CourseAssignmentSubmissionDetailScreen(
                    appViewModel(
                        backStackEntry, CourseAssignmentSubmissionDetailViewModel::class,
                            ::CourseAssignmentSubmissionDetailViewModel
                    )
                )
            }

            contentScene("/${ClazzLogEditAttendanceViewModel.DEST_NAME}") { backStackEntry ->
                ClazzLogEditAttendanceScreen(
                    appViewModel(backStackEntry, ClazzLogEditAttendanceViewModel::class,
                        ::ClazzLogEditAttendanceViewModel)
                )
            }


            contentScene("/${ClazzLogListAttendanceViewModel.DEST_NAME}") { backStackEntry ->
                ClazzLogListAttendanceScreen(
                    appViewModel(backStackEntry, ClazzLogListAttendanceViewModel::class,
                        ::ClazzLogListAttendanceViewModel)
                )
            }

            contentScene("/${CourseGroupSetListViewModel.DEST_NAME}") { backStackEntry ->
                CourseGroupSetListScreen(
                    appViewModel(backStackEntry, CourseGroupSetListViewModel::class,
                        ::CourseGroupSetListViewModel)
                )
            }

            contentScene("/${SettingsViewModel.DEST_NAME}") { backStackEntry ->
                SettingsScreen(
                    appViewModel(backStackEntry, SettingsViewModel::class, ::SettingsViewModel)
                )
            }

            contentScene("/${SiteDetailViewModel.DEST_NAME}") { backStackEntry ->
                SiteDetailScreen(
                    appViewModel(backStackEntry, SiteDetailViewModel::class, ::SiteDetailViewModel)
                )
            }

            contentScene("/${SiteEditViewModel.DEST_NAME}") { backStackEntry ->
                SiteEditScreen(
                    appViewModel(backStackEntry, SiteEditViewModel::class, ::SiteEditViewModel)
                )
            }

            contentScene("/${RegisterAgeRedirectViewModel.DEST_NAME}") { backStackEntry ->
                RegisterAgeRedirectScreen(
                    appViewModel(backStackEntry, RegisterAgeRedirectViewModel::class,
                        ::RegisterAgeRedirectViewModel)
                )
            }

            contentScene("/${SiteTermsDetailViewModel.DEST_NAME}") { backStackEntry ->
                SiteTermsDetailScreen(
                    appViewModel(backStackEntry, SiteTermsDetailViewModel::class, ::SiteTermsDetailViewModel)
                )
            }

            contentScene("/${RegisterMinorWaitForParentViewModel.DEST_NAME}") { backStackEntry ->
                RegisterMinorWaitForParentScreen(
                    appViewModel(backStackEntry, RegisterMinorWaitForParentViewModel::class,
                        ::RegisterMinorWaitForParentViewModel)
                )
            }

            contentScene("/${ParentalConsentManagementViewModel.DEST_NAME}") { backStackEntry ->
                ParentalConsentManagementScreen(
                    appViewModel(backStackEntry, ParentalConsentManagementViewModel::class,
                        ::ParentalConsentManagementViewModel)
                )
            }


            contentScene("/${ClazzLogEditViewModel.DEST_NAME}") { backStackEntry ->
                ClazzLogEditScreen(
                    appViewModel(backStackEntry, ClazzLogEditViewModel::class,
                        ::ClazzLogEditViewModel)
                )
            }

            contentScene("/${CourseTerminologyListViewModel.DEST_NAME}") { backStackEntry ->
                CourseTerminologyListScreen(
                    appViewModel(backStackEntry, CourseTerminologyListViewModel::class,
                        ::CourseTerminologyListViewModel)
                )
            }

            contentScene("/${CourseTerminologyEditViewModel.DEST_NAME}") { backStackEntry ->
                CourseTerminologyEditScreen(
                    appViewModel(backStackEntry, CourseTerminologyEditViewModel::class,
                        ::CourseTerminologyEditViewModel)
                )
            }

            contentScene("/${ContentEntryGetMetadataViewModel.DEST_NAME}") { backStackEntry ->
                ContentEntryGetMetadataScreen(
                    appViewModel(backStackEntry, ContentEntryGetMetadataViewModel::class,
                        ::ContentEntryGetMetadataViewModel)
                )
            }

            contentScene("/${ContentEntryEditViewModel.DEST_NAME}") { backStackEntry ->
                ContentEntryEditScreen(
                    appViewModel(backStackEntry, ContentEntryEditViewModel::class,
                        ::ContentEntryEditViewModel)
                )
            }
        }
    }
}