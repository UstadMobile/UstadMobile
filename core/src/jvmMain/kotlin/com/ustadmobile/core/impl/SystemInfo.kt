package com.ustadmobile.core.impl

actual fun getOs(): String = System.getProperty("os.name") ?: "jvm-unknown"

//TODO: separate out os version and type
actual fun getOsVersion(): String = System.getProperty("os.name") ?: "jvm-unknown"
