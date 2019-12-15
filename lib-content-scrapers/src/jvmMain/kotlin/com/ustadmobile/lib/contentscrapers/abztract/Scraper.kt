package com.ustadmobile.lib.contentscrapers.abztract

import java.io.File

abstract class Scraper {

    abstract fun isContentUpdated(): Boolean

    abstract fun scrapeUrl(startingUrl: String, tmpLocation: File)

}