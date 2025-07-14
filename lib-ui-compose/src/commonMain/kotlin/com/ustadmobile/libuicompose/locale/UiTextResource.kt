package com.ustadmobile.libuicompose.locale

import androidx.compose.runtime.Composable
import com.ustadmobile.core.impl.locale.StringResourceUiText
import com.ustadmobile.core.impl.locale.StringUiText
import com.ustadmobile.core.impl.locale.UiText
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun uiTextStringResource(uiText: UiText) : String {
    return when(uiText) {
        is StringUiText -> uiText.text
        is StringResourceUiText -> stringResource(uiText.resource)
    }
}
