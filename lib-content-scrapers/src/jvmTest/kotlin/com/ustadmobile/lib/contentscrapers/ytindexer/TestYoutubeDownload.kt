package com.ustadmobile.lib.contentscrapers.ytindexer

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class TestYoutubeDownload {

    private lateinit var db: UmAppDatabase
    private lateinit var containerDao: ContainerDao
    private lateinit var containerEntryDao: ContainerEntryDao
    private lateinit var containerEntryFileDao: ContainerEntryFileDao
    private lateinit var containerFolder: File
    private lateinit var entry: ContentEntry

    @Before
    fun setup() {
        ContentScraperUtil.checkIfPathsToDriversExist()
        db = UmAppDatabase.getInstance(Any())
       // db.clearAllTables()

        containerDao = db.containerDao
        containerEntryDao = db.containerEntryDao
        containerEntryFileDao = db.containerEntryFileDao

        containerFolder = Files.createTempDirectory("ytcontainer").toFile()

        entry = ContentEntry()
        entry.contentEntryUid = db.contentEntryDao.insert(entry)


    }


    @Test
    fun test(){

        var scraper = ChildYtIndexer(entry.contentEntryUid, 5, db)

        scraper.startPlayListIndexer("https://www.youtube.com/playlist?list=PLFhWybf5UzoyuW6kiro3Y4KtFmQpzxrLD", containerFolder)

    }


}