package com.ustadmobile.libcache.io

import kotlinx.io.RawSource

expect fun ByteArray.asKotlinxIoSource(): RawSource
