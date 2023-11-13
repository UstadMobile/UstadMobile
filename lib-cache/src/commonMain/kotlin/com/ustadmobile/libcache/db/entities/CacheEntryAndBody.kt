package com.ustadmobile.libcache.db.entities

import androidx.room.Embedded

data class CacheEntryAndBody(
    @Embedded
    var cacheEntry: CacheEntry? = null,
    @Embedded
    var responseBody: ResponseBody? = null,
) {
}