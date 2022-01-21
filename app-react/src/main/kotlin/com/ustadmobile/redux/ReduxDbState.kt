package com.ustadmobile.redux

import com.ustadmobile.core.db.UmAppDatabase
import redux.RAction

data class ReduxDbState(var db: UmAppDatabase? = null, var repo: UmAppDatabase ? = null): RAction