package com.ustadmobile.libuicompose.util.passkey

import androidx.compose.runtime.Composable
import com.ustadmobile.core.domain.passkey.PasskeyResult

@Composable
actual fun CreatePasskeyPrompt(
    username: String,
    personUid: String,
    doorNodeId: String,
    usStartTime: Long,
    serverUrl:String,
    passkeyData:(PasskeyResult)->Unit,
    passkeyError:(String)->Unit
) {
}