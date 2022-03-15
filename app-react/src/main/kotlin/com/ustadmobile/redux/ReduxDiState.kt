package com.ustadmobile.redux

import org.kodein.di.DI
import redux.RAction

data class ReduxDiState(var instance: DI = DI.lazy {  }): RAction