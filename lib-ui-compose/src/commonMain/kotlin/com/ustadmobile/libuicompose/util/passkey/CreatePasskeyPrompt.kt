package com.ustadmobile.libuicompose.util.passkey

import androidx.compose.runtime.Composable
import com.ustadmobile.core.domain.passkey.PasskeyData


@Composable
expect fun CreatePasskeyPrompt (
     username: String,
     personUid: String,
     doorNodeId: String,
     usStartTime: Long,
     serverUrl:String,
     passkeyData:(PasskeyData)->Unit,
     passkeyError:(String)->Unit
)