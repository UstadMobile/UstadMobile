package com.ustadmobile.libuicompose.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.Flow

private const val KEY_LAST_NAV_COMMAND_TIMESTAMP = "_last_nav_command_ts"

@Composable
fun NavCommandEffect(
    navController: UstadNavController,
    navCommandFlow: Flow<NavCommand>,
    savedStateHandle: UstadSavedStateHandle,
) {
    LaunchedEffect(navCommandFlow) {
        navCommandFlow.collect { navCommand ->
            /*
             * Android may retain the ViewModel in memory, which would lead to "replaying" a previous
             * navigation command (e.g. if the user goes back). we use the savedstatehandle to track
             * navigation commands that are executed and avoid this.
             */
            val lastCommandTimestamp = savedStateHandle[KEY_LAST_NAV_COMMAND_TIMESTAMP]?.toLong() ?: 0
            if(navCommand.timestamp <= lastCommandTimestamp)
                return@collect

            //Adapt and give to NavHostController
            savedStateHandle[KEY_LAST_NAV_COMMAND_TIMESTAMP] = navCommand.timestamp.toString()
            when(navCommand) {
                is NavigateNavCommand -> {
                    navController.navigate(
                        viewName = navCommand.viewName,
                        args = navCommand.args,
                        goOptions = navCommand.goOptions
                    )
                }
                is PopNavCommand -> {
                    navController.popBackStack(
                        viewName = navCommand.viewName,
                        inclusive = navCommand.inclusive
                    )
                }
                else -> {
                    //do nothing
                }
            }
        }
    }
}