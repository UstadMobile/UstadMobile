package com.ustadmobile.core.util.ext

import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.tincan.UmAccountGroupActor
import com.ustadmobile.lib.db.entities.LearnerGroupMemberWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount



fun UmAccount?.toXapiActorJsonObject(context: Any) : UmAccountActor {
    val server = this?.endpointUrl ?: "http://localhost"
    return UmAccountActor(account = UmAccountActor.Account(homePage = server, name = this?.username ?: "anonymous"))
}

fun UmAccount?.toXapiGroupJsonObject(memberList: List<LearnerGroupMemberWithPerson>): UmAccountGroupActor {
    val endpoint = this?.endpointUrl ?: "http://localhost"
    return UmAccountGroupActor(
            account = UmAccountActor.Account(homePage = endpoint, name = "group:${memberList[0].learnerGroupMemberLgUid}"),
            members = memberList.map { member ->
                UmAccountActor(account = UmAccountActor.Account(homePage = endpoint, name = member.person?.username ?: "anonymous"))
            })
}

val UmAccount.userAtServer: String
    get() = "$username@$endpointUrl"

fun UmAccount.asPerson(admin: Boolean = false) = Person().also {
    it.firstNames = firstName
    it.lastName = lastName
    it.personUid = personUid
    it.admin = admin
}