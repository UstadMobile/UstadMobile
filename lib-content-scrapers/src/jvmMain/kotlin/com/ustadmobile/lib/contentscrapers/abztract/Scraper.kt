package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.container.ContainerManager
import java.io.File

abstract class Scraper(val containerManager: ContainerManager) {

    abstract fun isContentUpdated(): Boolean

    abstract fun scrapeUrl(url: String, tmpLocation: File)

    abstract fun close()

}