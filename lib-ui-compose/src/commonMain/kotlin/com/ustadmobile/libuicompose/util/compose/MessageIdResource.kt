package com.ustadmobile.libuicompose.util.compose

import androidx.compose.runtime.Composable
import com.ustadmobile.core.util.MessageIdOption2
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun stringIdMapResource(map: Map<Int, StringResource>, key: Int) : String{
    return stringResource(map[key] ?: throw IllegalArgumentException("Not found: $key"))
}

@Composable
fun stringIdOptionListResource(
    options: List<MessageIdOption2>,
    key: Int
): String {
    return stringResource(options.firstOrNull { it.value == key }?.stringResource
        ?: MR.strings.error)
}
