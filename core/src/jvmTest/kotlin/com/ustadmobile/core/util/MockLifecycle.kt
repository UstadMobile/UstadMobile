package com.ustadmobile.core.util

import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.Lifecycle
import com.ustadmobile.door.lifecycle.LifecycleOwner
import org.mockito.kotlin.mock

fun mockLifecycleOwner(state: DoorState) = mock<LifecycleOwner>{
    val mockLifecycle = mock<Lifecycle> {
        on { realCurrentDoorState }.thenReturn(state)
    }
    on { getLifecycle() }.thenReturn(mockLifecycle)
}
