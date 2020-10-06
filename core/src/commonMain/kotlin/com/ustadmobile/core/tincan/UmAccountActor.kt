package com.ustadmobile.core.tincan

import kotlinx.serialization.Serializable

/**
 * This is a minimal data class that represents a UmAccount object as an Xapi actor object
 * e.g.
 *
 * { objectType: "Actor", account: { homePage : "http://endpoint/url", username: "accountUsername"} }
 */


@Serializable
class UmAccountActor(val objectType: String = "Actor", val account: Account = Account()) {

    @Serializable
    class Account(val homePage: String = "", val name: String = "")

}

@Serializable
class UmAccountGroupActor(val objectType: String = "Group", val account: UmAccountActor.Account = UmAccountActor.Account(), val members: List<UmAccountActor> = listOf())


