package com.ustadmobile.libuicompose.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import kotlinx.coroutines.flow.Flow


@Composable
fun NavCommandEffect(
    navController: UstadNavControllerPreCompose,
    navCommandFlow: Flow<NavCommand>,
) {
    LaunchedEffect(navCommandFlow) {
        navCommandFlow.collect { navCommand ->
            /*
             * Android may retain the ViewModel in memory, which would lead to "replaying" a previous
             * navigation command (e.g. if the user goes back). we use the savedstatehandle to track
             * navigation commands that are executed and avoid this.
             */
            navController.onCollectNavCommand(navCommand)
        }
    }
}