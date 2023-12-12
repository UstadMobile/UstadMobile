package com.ustadmobile.libuicompose.util.compose

import androidx.compose.runtime.Composable
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun yesNoStringResource(
    yes: Boolean,
): String = stringResource(if(yes) { MR.strings.yes } else { MR.strings.no })
