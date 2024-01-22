package com.ustadmobile.core.util

import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import kotlinx.io.files.Path
import org.junit.rules.TemporaryFolder

fun newTestUstadCache(temporaryFolder: TemporaryFolder): UstadCache {
    return UstadCacheBuilder(
        dbUrl = "jdbc:sqlite::memory:",
        storagePath = Path(temporaryFolder.newFolder().absolutePath),
    ).build()
}
