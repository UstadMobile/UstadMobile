package com.ustadmobile.core.util.ext

import android.accounts.Account
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManagerAndroid

fun UserSessionWithPersonAndEndpoint.toAndroidAccount() : Account {
    val displayUrl = endpoint.url
        .removePrefix("http://")
        .removePrefix("https://")
        .removeSuffix("/")
    return Account("${this.person.username}@$displayUrl", UstadAccountManagerAndroid.ACCOUNT_TYPE)
}