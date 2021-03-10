package com.ustadmobile.core.container

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import org.junit.Assert
import java.io.IOException


object TestContainer{

    fun assertContainersHaveSameContent(containerUid: Long, db: UmAppDatabase, repo: UmAppDatabase) {

        val repoContainer = repo.containerDao.findByUid(containerUid)!!
        val repoManager = ContainerManager(repoContainer, db, repo)

        val dbContainer = db.containerDao.findByUid(containerUid)!!
        val dbManager = ContainerManager(dbContainer, db, repo)

        Assert.assertEquals("Containers have same number of entries",
                repoContainer.cntNumEntries.toLong(),
                db.containerEntryDao.findByContainer(containerUid).size.toLong())

        for (entry in repoManager.allEntries) {
            val entry2 = dbManager.getEntry(entry.cePath!!)
            Assert.assertNotNull("Client container also contains " + entry.cePath!!,
                    entry2)

            val e1Contents: ByteArray
            try {
                e1Contents = UMIOUtils.readStreamToByteArray(repoManager.getInputStream(entry))
            } catch (e1: Exception) {
                throw IOException("Exception reading entry 1 ${entry.cePath} from ${entry.containerEntryFile?.cefPath}",
                        e1)
            }


            val e2Contents: ByteArray

            try {
                e2Contents = UMIOUtils.readStreamToByteArray(dbManager.getInputStream(entry2!!))
            } catch (e2: Exception) {
                throw IOException("Exception reading entry 2 ${entry.cePath} from ${entry2!!.containerEntryFile?.cefPath}",
                        e2)
            }

            Assert.assertArrayEquals("${entry.cePath} contents are the same",
                    e1Contents, e2Contents)
        }
    }

}