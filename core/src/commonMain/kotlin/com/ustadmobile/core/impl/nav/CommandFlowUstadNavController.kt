package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

/**
 * UstadNavController implementation that will emit navigation events through a flow, which is then
 * observed while a lifecycle is active. This avoids the ViewModel having a reference to the Context,
 * which in turn would lead to memory leaks. This also avoids a ViewModel triggering navigation for
 * a screen which is not active.
 */
class CommandFlowUstadNavController: UstadNavController {

    private val _commandFlow = MutableSharedFlow<NavCommand>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val commandFlow: Flow<NavCommand> = _commandFlow.asSharedFlow()

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        _commandFlow.tryEmit(NavigateNavCommand(viewName, args, goOptions))
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        _commandFlow.tryEmit(PopNavCommand(viewName, inclusive))
    }

    override val currentBackStackEntry: UstadBackStackEntry
        get() = throw IllegalStateException("getting current back stack entry NOT supported by commadnflownavcontroller")

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        throw IllegalStateException("getting back stack entry ($viewName) NOT supported by commadnflownavcontroller")
    }
}
