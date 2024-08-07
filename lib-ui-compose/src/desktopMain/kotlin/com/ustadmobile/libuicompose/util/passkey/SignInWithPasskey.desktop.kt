package com.ustadmobile.libuicompose.util.passkey

import androidx.compose.runtime.Composable
import com.ustadmobile.core.domain.passkey.PassKeySignInData


@Composable
actual fun SignInWithPasskey(
    onSignInWithPasskey: (PassKeySignInData) -> Unit,
) {
}