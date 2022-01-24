package com.ustadmobile.core.impl

import android.os.Build


actual fun getOs(): String = "Android"

actual fun getOsVersion(): String = Build.VERSION.RELEASE ?: Build.VERSION.CODENAME ?: "Unknown"
