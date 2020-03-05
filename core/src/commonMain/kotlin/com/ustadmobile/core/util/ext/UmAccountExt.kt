package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.lib.db.entities.UmAccount

fun UmAccount?.toXapiActorJsonObject(context: Any) : UmAccountActor {
    val server = this?.endpointUrl ?: UmAccountManager.getActiveEndpoint(context)
    return UmAccountActor(account = UmAccountActor.Account(homePage = server, username = this?.username ?: "anonymous"))
}