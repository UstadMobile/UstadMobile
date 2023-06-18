package com.ustadmobile.core.util

import org.kodein.di.DI

expect fun isValidPhoneNumber(di: DI, str: String = ""): Boolean