package com.ustadmobile.redux

import com.ustadmobile.navigation.UstadBackStackEntryJs
import redux.RAction

data class ReduxNavStackState(var stack: MutableList<UstadBackStackEntryJs> = mutableListOf()): RAction
