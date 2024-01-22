package com.ustadmobile.libuicompose.components.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerWebClient
import com.ustadmobile.core.util.ext.onActiveEndpoint
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

@Composable
actual fun rememberContentEntryVersionNavigator(
    contentEntryVersionUid: Long
): UstadWebViewNavigator {
    val di = localDI()
    return remember(contentEntryVersionUid) {
        UstadWebViewNavigatorAndroid(
            webViewClient = ContentEntryVersionServerWebClient(
                useCase = di.onActiveEndpoint().direct.instance(),
                contentEntryVersionUid = contentEntryVersionUid,
            ),
        )
    }
}