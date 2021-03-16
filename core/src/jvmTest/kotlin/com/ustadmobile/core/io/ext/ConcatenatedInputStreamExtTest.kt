package com.ustadmobile.core.io.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.linkExistingContainerEntries
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.concurrent.atomic.AtomicLong

@Suppress("BlockingMethodInNonBlockingContext")
class ConcatenatedInputStreamExtTest {

    @Rule
    @JvmField
    val ustadTestRule = UstadTestRule()

    lateinit var di: DI

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    /**
     * This is used so that we can get a completely different database using the same di. The test
     * will use the source endpoint to generate a concatenatedentry stream
     */
    val sourceEndpoint = Endpoint("http://source.endpoint/")

    val destEndpoint = Endpoint("http://dest.endpoint/")

    lateinit var sourceDb: UmAppDatabase

    lateinit var sourceRepo: UmAppDatabase

    lateinit var destDb: UmAppDatabase

    lateinit var destRepo: UmAppDatabase

    lateinit var container: Container

    lateinit var entriesToWrite: List<ContainerEntryWithMd5>

    @Suppress("BlockingMethodInNonBlockingContext")
    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        sourceDb = di.direct.on(sourceEndpoint).instance(tag = DoorTag.TAG_DB)
        sourceRepo = di.direct.on(sourceEndpoint).instance(tag = DoorTag.TAG_REPO)

        destDb = di.direct.on(destEndpoint).instance(tag = DoorTag.TAG_DB)
        destRepo = di.direct.on(destEndpoint).instance(tag = DoorTag.TAG_REPO)

        container = Container().apply {
            containerUid = sourceRepo.containerDao.insert(this)
        }

        runBlocking {
            sourceRepo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                    "/com/ustadmobile/core/contentformats/epub/test.epub",
                    ContainerAddOptions(storageDirUri = temporaryFolder.newFolder().toDoorUri()))
        }

        entriesToWrite = sourceDb.containerEntryDao.findByContainer(container.containerUid)
                .map { it.toContainerEntryWithMd5() }
    }

    @Test
    fun givenValidStream_whenReadAndSaveCalled_thenShouldBeTheSameInOtherDb() {
        val entryOut = ByteArrayOutputStream()
        val md5s = entriesToWrite.mapNotNull { it.cefMd5?.base64EncodedToHexString() }
        val concatResponse = sourceDb.containerEntryFileDao.generateConcatenatedFilesResponse2(
                md5s.joinToString(separator = ";"), mapOf(), sourceDb)
        concatResponse.writeTo(entryOut)
        entryOut.flush()

        val concatIn = ConcatenatedInputStream2(ByteArrayInputStream(entryOut.toByteArray()))
        runBlocking {
            concatIn.readAndSaveToDir(temporaryFolder.newFolder(), temporaryFolder.newFolder(),
                    destDb, AtomicLong(), entriesToWrite, md5s.toMutableList(), "concattest")
        }

        sourceDb.assertContainerEqualToOther(container.containerUid, destDb)
    }

    @Test
    fun givenFirstStreamCorrupted_whenReadAndSaveCalledTwice_thenCanResumeAndSucceed() {
        for(j in 0 .. 1) {
            val concatOut = ByteArrayOutputStream()
            val entriesRemaining = runBlocking {
                destDb.linkExistingContainerEntries(container.containerUid, entriesToWrite)
            }.entriesWithoutMatchingFile.sortedBy { it.cefMd5 }

            val md5s = entriesRemaining.mapNotNull { it.cefMd5?.base64EncodedToHexString() }
            val concatResponse = sourceDb.containerEntryFileDao.generateConcatenatedFilesResponse2(
                    md5s.joinToString(separator = ";"), mapOf(), sourceDb)
            concatResponse.writeTo(concatOut)
            concatOut.flush()

            val concatBytes = concatOut.toByteArray()

            //if this is the first loop through, simulate corrupting some data
            if(j == 0) {
                val midPoint = concatBytes.size / 2
                for(i in midPoint .. (midPoint + 200000)) {
                    concatBytes[i] = 0
                }
            }

            val concatIn = ConcatenatedInputStream2(ByteArrayInputStream(concatBytes))
            try {
                runBlocking {
                    concatIn.readAndSaveToDir(temporaryFolder.newFolder(), temporaryFolder.newFolder(),
                            destDb, AtomicLong(), entriesToWrite, md5s.toMutableList(), "concattest")
                }
            }catch(e: Exception) {
                e.printStackTrace()
                //OK... that's expected (once)
            }
        }

        sourceDb.assertContainerEqualToOther(container.containerUid, destDb)
    }


}