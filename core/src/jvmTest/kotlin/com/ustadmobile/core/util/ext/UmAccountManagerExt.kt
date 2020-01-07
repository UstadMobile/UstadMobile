package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.util.test.ext.bindJndiForActiveEndpoint

fun UmAccountManager.bindDbForActiveContext(context: Any)
        = bindJndiForActiveEndpoint(getActiveEndpoint(context))