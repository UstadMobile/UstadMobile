package com.ustadmobile.model.statemanager

import redux.RAction

/**
 * Represent hash property that can be used to determine if it was a component refresh
 */
data class HashState(val view: String = ""): RAction
