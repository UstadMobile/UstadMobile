package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

@Composable
fun rememberActiveDatabase(
    di: DI? = rememberDi(),
    accountManager: UstadAccountManager? = rememberAccountManager(di = di),
    tag: Int = DoorTag.TAG_DB
) : UmAppDatabase? {
    val activeEndpoint = accountManager?.activeEndpoint
    return remember(activeEndpoint, tag) {
        activeEndpoint?.let { di?.direct?.on(it)?.instance(tag = tag) }
    }
}
