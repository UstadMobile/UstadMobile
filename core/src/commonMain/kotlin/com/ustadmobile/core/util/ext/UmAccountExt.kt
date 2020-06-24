package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.lib.db.entities.UmAccount

fun UmAccount?.toXapiActorJsonObject(context: Any) : UmAccountActor {
    val server = this?.endpointUrl ?: UmAccountManager.getActiveEndpoint(context)
    return UmAccountActor(account = UmAccountActor.Account(homePage = server, name = this?.username ?: "anonymous"))
}

val UmAccount.userAtServer: String
    get() = "$username@$endpointUrl"
