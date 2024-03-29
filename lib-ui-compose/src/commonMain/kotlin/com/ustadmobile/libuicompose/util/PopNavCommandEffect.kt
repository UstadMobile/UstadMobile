package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.libuicompose.util.ext.ustadDestName
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator

data class PopTargetState(
    val command: PopNavCommand,
    val targetHit: Boolean = false,
)

/**
 * Effect function that will handle popUpTo navigation that is not supported by PreCompose. PreCompose
 * supports popUpTo on navigate, but not on just pop itself.
 *
 * This will collect a pop command from a flow. When a command is collected, the effect will call
 * navigator.goBack() until it reaches the target or it is no longer possible to go back.
 *
 * onSetContentVisible will be called and set to false before starting to go back, and then will be
 * called once done and set to true. This can be used to avoid screens flashing rapidly in front
 * of the user.
 */
@Composable
fun PopNavCommandEffect(
    navigator: Navigator,
    popCommandFlow: Flow<PopNavCommand>,
    onSetContentVisible: (Boolean) -> Unit,
) {
    var popUpToTarget: PopTargetState? by remember {
        mutableStateOf(null)
    }

    var pendingGoBackTimeout: Job? by remember {
        mutableStateOf(null)
    }

    var targetHit by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(popCommandFlow) {
        popCommandFlow.collect {
            Napier.d { "UstadNavControllerNavHost: received PopNavCommand $it" }
            onSetContentVisible(false)
            targetHit = false
            popUpToTarget = PopTargetState(it)
        }
    }

    val currentEntry by navigator.currentEntry.collectAsState(null)
    val canGoBack by navigator.canGoBack.collectAsState(true)

    LaunchedEffect(popUpToTarget, currentEntry) {
        val currentDestName = currentEntry?.ustadDestName
        val popUpToTargetVal = popUpToTarget
        val logPrefix = {
            "UstadNavControllerNavHost: currentDestName=$currentDestName target = $popUpToTargetVal"
        }

        //Unfortunately if we call go back immediately in succession, that will not
        //work, so we have to retry. This will be cancelled by the effect once
        //navigation has actually taken place, so there is no risk of running goBack
        //too many times.
        fun launchAttemptToGoBack() = coroutineScope.launch {
            repeat(3) {
                Napier.d { "${logPrefix()} target not reached: can go back,: attempting" }
                navigator.goBack()
                delay(200)
            }
        }

        pendingGoBackTimeout?.also {
            it.cancel()
            Napier.v { "${logPrefix()} cancel timeout"}
        }

        when {
            /*
             * There is a popUpToTarget, but we have not reached it yet. Go back once more
             */
            popUpToTargetVal != null && currentDestName != popUpToTargetVal.command.viewName
                    && !targetHit -> {
                if(canGoBack){
                    Napier.d { "${logPrefix()} target not reached: can go back, going back" }
                    pendingGoBackTimeout = launchAttemptToGoBack()
                }else {
                    //Target is not in stack, cannot go back, give up
                    Napier.d { "${logPrefix()} target not reached: but cannot go back, give up" }
                    popUpToTarget = null
                    onSetContentVisible(true)
                }
            }
            /*
             * There is a popUpToTarget an we have now reached the target
             */
            popUpToTargetVal != null && currentDestName == popUpToTargetVal.command.viewName -> {
                if(popUpToTargetVal.command.inclusive) {
                    Napier.d { "${logPrefix()} target reached, popup is inclusive." }
                    targetHit = true
                    pendingGoBackTimeout = launchAttemptToGoBack()
                }else {
                    Napier.d { "${logPrefix()} target reached, popup is not inclusive. Done. Set content visible" }
                    popUpToTarget = null
                    onSetContentVisible(true)
                }
            }

            /*
             * There was a popUpToTarget where inclusive was true, that has now also been popped.
             */
            popUpToTargetVal != null && currentDestName != popUpToTargetVal.command.viewName &&
                    targetHit -> {
                Napier.d { "${logPrefix()} target was hit, popup was inclusive, time to show content" }
                popUpToTarget = null
                onSetContentVisible(true)
            }
        }
    }

}