package com.ustadmobile.core.torrent

import bt.torrent.maker.TorrentBuilder
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import java.nio.file.Paths
import java.util.*

class TestSeedManager {

    private lateinit var di: DI

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()


    private lateinit var db: UmAppDatabase


    private lateinit var repo: UmAppDatabase

    private lateinit var seedManager: SeedManager

    private lateinit var containerDir: File


    @Before
    fun setup() {

        containerDir = temporaryFolder.newFolder("container")

        di = DI {
            import(ustadTestRule.diModule)
            bind<File>(tag = DiTag.TAG_TORRENT_DIR) with scoped(ustadTestRule.endpointScope).singleton {
                temporaryFolder.newFolder("torrent")
            }
            bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(ustadTestRule.endpointScope).singleton {
                containerDir
            }
            bind<SeedManager>() with scoped(ustadTestRule.endpointScope).singleton {
                SeedManagerImpl(endpoint = context, di = di)
            }
        }


        val accountManager: UstadAccountManager by di.instance()
        accountManager.activeEndpoint = Endpoint("http://localhost:8089/")
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        repo = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_REPO)
        seedManager = di.on(accountManager.activeEndpoint).direct.instance()

        val container = Container().apply {
            containerUid = repo.containerDao.insert(this)
        }

        runBlocking {
            repo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                    "/com/ustadmobile/core/contentformats/epub/test.epub",
                    ContainerAddOptions(containerDir.toDoorUri()))

            val torrentDir = di.on(accountManager.activeEndpoint).direct.instance<File>(tag = DiTag.TAG_TORRENT_DIR)
            val torrentFile = File(torrentDir, "${container.containerUid}.torrent")

            val fileList = db.containerEntryDao.findByContainer(container.containerUid)

            val result = TorrentBuilder()
                    .rootPath(containerDir.toPath())
                    .addFiles(*fileList.map { Paths.get(it.containerEntryFile!!.cefPath) }.toTypedArray())
                    .createdBy("UstadMobile")
                    .creationDate(Date())
                    .announce("http://192.168.1.118:8000/announce")
                    .build()

            torrentFile.writeBytes(result)

        }
    }


    @Test
    fun test(){

        runBlocking {
            seedManager.start()
        }




    }

}