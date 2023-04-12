package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.account.UstadAccountManager
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

@Composable
fun rememberAccountManager(di: DI?): UstadAccountManager? {
    return remember(di) {
        di?.direct?.instance()
    }
}
