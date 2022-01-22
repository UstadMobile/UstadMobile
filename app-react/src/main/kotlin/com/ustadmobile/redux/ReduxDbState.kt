package com.ustadmobile.redux

import com.ustadmobile.core.db.UmAppDatabase
import redux.RAction

data class ReduxDbState(var instance: UmAppDatabase? = null): RAction