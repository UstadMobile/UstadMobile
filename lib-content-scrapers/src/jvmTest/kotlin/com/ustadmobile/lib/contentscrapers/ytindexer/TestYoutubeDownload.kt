package com.ustadmobile.lib.contentscrapers.ytindexer

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

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
        val nodeIdAndAuth = NodeIdAndAuth(
            Random.nextInt(0, Int.MAX_VALUE),
            randomUuid().toString())
        db = UmAppDatabase.getInstance(Any(), nodeIdAndAuth, true)
       // db.clearAllTables()

        containerDao = db.containerDao
        containerEntryDao = db.containerEntryDao
        containerEntryFileDao = db.containerEntryFileDao

        containerFolder = Files.createTempDirectory("ytcontainer").toFile()

        entry = ContentEntry()
        entry.contentEntryUid = db.contentEntryDao.insert(entry)


    }


}