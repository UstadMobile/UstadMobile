package com.ustadmobile.core.util

import kotlin.js.Date

actual fun getSystemTimeInMillis() = Date().getTime().toLong()
