package com.ustadmobile.model.statemanager

import redux.RAction

/**
 * State action which handles toolbar tabs
 */
data class ToolbarTabs(var labels: List<String> = listOf(), var keys: List<String> = listOf(),
                       var selected:Any? = null, var onTabChange:(Any)-> Unit = {}): RAction
