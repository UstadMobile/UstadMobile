package com.ustadmobile.core.util.ext

import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import com.ustadmobile.lib.db.entities.LearnerGroupMemberWithPerson
import com.ustadmobile.lib.db.entities.UmAccount

fun UmAccount?.toXapiActorJsonObject(context: Any) : UmAccountActor {
    val server = this?.endpointUrl ?: "http://localhost"
    return UmAccountActor(account = UmAccountActor.Account(homePage = server, name = this?.username ?: "anonymous"))
}

fun UmAccount?.toXapiGroupJsonObject(memberList: List<LearnerGroupMemberWithPerson>): Actor {
    val endpoint = this?.endpointUrl ?: "http://localhost"
    return Actor().apply {
        objectType = "Group"
        account = Account().apply {
            homePage = endpoint
            name = "group:${memberList[0].learnerGroupMemberLgUid}"
        }
        members = memberList.map { member ->
            Actor().apply {
                objectType = "Agent"
                account = Account().apply {
                    homePage = endpoint
                    name = member.person?.username ?: "anonymous"
                 }
            }
        }
    }
}

val UmAccount.userAtServer: String
    get() = "$username@$endpointUrl"
