package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.UstadAccountManager
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

fun DI.onActiveEndpoint() = this.on(this.direct.instance<UstadAccountManager>().activeLearningSpace)
