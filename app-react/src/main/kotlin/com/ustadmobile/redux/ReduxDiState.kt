package com.ustadmobile.redux

import org.kodein.di.DI
import redux.RAction

data class ReduxDiState(var di: DI = DI.lazy {  }): RAction