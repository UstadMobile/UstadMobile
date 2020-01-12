package com.ustadmobile.sharedse.test.util

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.util.test.ext.bindJndiForActiveEndpoint

fun UmAccountManager.bindDbForActiveContext(context: Any)
        = bindJndiForActiveEndpoint(getActiveEndpoint(context))