package com.ustadmobile.hooks

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import react.useMemo

val URI_NOT_READY: DoorUri by lazy {
    DoorUri.parse("notready:///")
}

/**
 * Shorthand to collect a flow and return a usable href for images etc.
 */
fun <T> Flow<T>?.collectAttachmentUriSrc(
    initialState: DoorUri? = URI_NOT_READY,
    revokeOnCleanup: Boolean = false,
    uriFieldBlock: (T) -> String?,
): DoorUri? {
    val uriFlow = useMemo(this) {
        this?.map(uriFieldBlock) ?: flowOf(null)
    }

    val uri: String? by uriFlow.collectAsState(initialState?.toString())
    return useAttachmentUriSrc(uri, revokeOnCleanup)
}
