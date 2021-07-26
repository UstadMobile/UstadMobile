package com.ustadmobile.redux

import kotlinx.serialization.KSerializer
import redux.RAction

data class ReduxSerializationState(var serializer: KSerializer<*>? = null): RAction
