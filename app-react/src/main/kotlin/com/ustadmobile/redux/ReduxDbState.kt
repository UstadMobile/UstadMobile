package com.ustadmobile.redux

import com.ustadmobile.core.db.UmAppDatabase
import org.kodein.di.DI
import redux.RAction

data class ReduxDbState(var instance: UmAppDatabase? = null): RAction