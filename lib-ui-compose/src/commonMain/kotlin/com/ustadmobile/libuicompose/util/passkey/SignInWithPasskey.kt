package com.ustadmobile.libuicompose.util.passkey

import androidx.compose.runtime.Composable
import com.ustadmobile.core.domain.passkey.PassKeySignInData

@Composable
expect fun SignInWithPasskey(
    onSignInWithPasskey: (PassKeySignInData) -> Unit,
)