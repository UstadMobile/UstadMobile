package com.ustadmobile.lib.contentscrapers.abztract

import java.io.File

abstract class Scraper(val containerDir: File) {

    abstract fun isContentUpdated(): Boolean

    abstract fun scrapeUrl(url: String, tmpLocation: File)

    abstract fun close()

}