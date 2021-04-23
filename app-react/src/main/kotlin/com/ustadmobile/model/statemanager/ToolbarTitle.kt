package com.ustadmobile.model.statemanager

import redux.RAction

/**
 * State action which handles the toolbar title
 */
data class ToolbarTitle(var title: String): RAction
