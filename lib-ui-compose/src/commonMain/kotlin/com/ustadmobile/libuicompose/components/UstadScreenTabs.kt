package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.nav.UstadSavedStateHandlePreCompose
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackStackEntry
import org.kodein.di.DI
import kotlin.reflect.KClass

class TabScope(
    private val savedStateHandle: UstadSavedStateHandlePreCompose,
    private val backStackEntry: BackStackEntry,
    private val navController: UstadNavControllerPreCompose,
    private val onSetAppUiState: (AppUiState) -> Unit,
    private val navResultReturner: NavResultReturner,
    private val onShowSnackBar: SnackBarDispatcher,
) {

    /**
     * Create a view model for the tab which is in scope.
     */
    @Composable
    fun <T: UstadViewModel> tabViewModel(
        viewModelClass: KClass<T>,
        tab: TabItem,
        appUiStateMap: ((AppUiState) -> AppUiState)? = null,
        creator: (DI, UstadSavedStateHandle) -> T,
    ): T {
        return ustadViewModel(
            modelClass = viewModelClass,
            backStackEntry = backStackEntry,
            navController = navController,
            onSetAppUiState = onSetAppUiState,
            onShowSnackBar =  onShowSnackBar,
            navResultReturner = navResultReturner,
            appUiStateMap =  appUiStateMap,
            savedStateHandle = UstadSavedStateHandlePreCompose(
                savedStateHolder = backStackEntry.savedStateHolder,
                argsMap = tab.args.map { it.key to listOf(it.value) }.toMap(),
                savedKeys = savedStateHandle.savedKeys,
            ),
            block = creator,
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UstadScreenTabs(
    tabs: List<TabItem>,
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    navResultReturner: NavResultReturner,
    onShowSnackBar: SnackBarDispatcher,
    scrollable: Boolean = false,
    autoHideIfOneTab: Boolean = false,
    content: @Composable TabScope.(TabItem) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )

    val tabAppStateMap = remember {
        MutableStateFlow(mapOf<Int, AppUiState>())
    }

    var appBarHidden by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        tabAppStateMap.map { it[pagerState.currentPage] }.filterNotNull().collect {
            onSetAppUiState(it)
            appBarHidden = it.hideAppBar
            pagerState.canScrollForward
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val savedStateKeys: MutableMap<String, UstadSavedStateHandlePreCompose.SavedEntry> = remember {
        mutableMapOf()
    }

    val tabsHidden = appBarHidden || (autoHideIfOneTab && tabs.size <= 1)

    Column(
        modifier = Modifier.testTag("ustad_screen_tabs")
    ) {
        if (tabs.isNotEmpty()) {
            val tabContent : @Composable () -> Unit = {
                tabs.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = {
                            coroutineScope.launch { pagerState.scrollToPage(index) }
                        },
                        text = {
                            Text(
                                tabItem.label,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            if(scrollable && !tabsHidden) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ){
                    tabContent()
                }
            }else if(!tabsHidden) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage
                ) {
                    tabContent()
                }
            }


            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                userScrollEnabled = !appBarHidden,
            ) { tabIndex ->
                val selectedTab = tabs[tabIndex]
                val tabScope = TabScope(
                    savedStateHandle = UstadSavedStateHandlePreCompose(
                        savedStateHolder = backStackEntry.savedStateHolder,
                        argsMap = selectedTab.args.map { it.key to listOf(it.value) }.toMap(),
                        savedKeys = savedStateKeys,
                    ),
                    backStackEntry = backStackEntry,
                    navController = navController,
                    onSetAppUiState = {
                        tabAppStateMap.update { prev ->
                            prev.toMutableMap().apply {
                                put(tabIndex, it)
                            }.toMap()
                        }
                    },
                    navResultReturner = navResultReturner,
                    onShowSnackBar = onShowSnackBar,
                )

                content(tabScope, selectedTab)
            }
        }
    }
}