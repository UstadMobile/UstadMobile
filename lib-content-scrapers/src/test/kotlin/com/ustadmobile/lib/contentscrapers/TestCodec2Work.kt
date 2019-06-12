package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry

import org.junit.Test

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class TestCodec2Work {


    private fun initDb() {


        val db = UmAppDatabase.getInstance(null)
        db.clearAllTables()
        val repo = db.getRepository("https://localhost", "")

        val containerDoa = repo.containerDao
        val contentEntryDao = repo.contentEntryDao
        val containerEntryDao = db.containerEntryDao
        val containerEntryFileDao = db.containerEntryFileDao

        val entry = ContentEntry()
        entry.sourceUrl = "khan-id://1233"
        entry.entryId = "abc"
        entry.publisher = "Khan Academy"
        entry.contentEntryUid = 10
        contentEntryDao.insert(entry)

        val container = Container()
        container.mimeType = "video/mp4"
        container.containerUid = 5
        container.containerContentEntryUid = 10
        containerDoa.insert(container)

        val containerEntry = ContainerEntry()
        containerEntry.ceContainerUid = 5
        containerEntry.cePath = "images/abc.mp4"
        containerEntry.ceCefUid = 6
        containerEntry.ceUid = 7
        containerEntryDao.insert(containerEntry)

        val containerEntryFile = ContainerEntryFile()
        containerEntryFile.cefUid = 11
        containerEntryFile.cefPath = "1233"
        containerEntryFileDao.insert(containerEntryFile)


    }

    @Test
    fun test() {

        initDb()

        val khanfolder = File("D:\\content\\test-khan\\en\\")
        val containerFolder = File("D:\\content\\test-container\\")

        Codec2KhanWork.main(arrayOf(khanfolder.path, containerFolder.path))


    }

    @Test
    fun test2() {

        val path = Paths.get("container", "container.xml")


    }


}
