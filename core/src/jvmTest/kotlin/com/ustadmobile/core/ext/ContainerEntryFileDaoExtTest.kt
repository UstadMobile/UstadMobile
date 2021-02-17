package com.ustadmobile.core.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addDirToContainer
import com.ustadmobile.core.io.ext.kmpUri
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import java.io.File

class ContainerEntryFileDaoExtTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    lateinit var tmpFile1: File

    lateinit var tmpFile2: File

    lateinit var tmpDir: File

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        tmpDir = temporaryFolder.newFolder()

        tmpFile1 = File(tmpDir, "cat-pic0.jpg")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic0.jpg")
                .writeToFile(tmpFile1)

        tmpFile2 = File(tmpDir, "cat-pic1.jpg")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic1.jpg")
                .writeToFile(tmpFile2)


    }

    @Test
    fun givenContainerWithEntries_whenGenerateConcatenatedResponseCalled_thenShouldWriteData() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val container = Container().apply {
            this.containerUid = repo.containerDao.insert(this)
        }

        val storageDir = temporaryFolder.newFolder()

        runBlocking {
            repo.addDirToContainer(container.containerUid, tmpDir.kmpUri, true,
                    ContainerAddOptions(storageDir.kmpUri, false))
        }

    }

}