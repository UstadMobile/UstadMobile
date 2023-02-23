package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ustadmobile.port.android.generated.MessageIDMap

@Composable
fun messageIdResource(id: Int): String {
    return stringResource(id = MessageIDMap.ID_MAP[id]
        ?: throw IllegalArgumentException("Invalid messageId: $id")
    )
}

@Composable
fun messageIdMapResource(map: Map<Int, Int>, key: Int): String {
    return messageIdResource(id = map[key] ?: 0)
}
