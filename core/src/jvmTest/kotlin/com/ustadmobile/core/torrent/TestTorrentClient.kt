package com.ustadmobile.core.torrent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import java.net.InetAddress

class TestTorrentClient {

    private lateinit var localDi: DI

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()


    @Before
    fun setup() {

        val clientFolder = temporaryFolder.newFolder("clientFolder")
        val torrentClientFolder =  File(clientFolder, "torrent")
        torrentClientFolder.mkdirs()

        val containerClientFolder =  File(clientFolder, "container")
        containerClientFolder.mkdirs()

        localDi = DI {
            import(ustadTestRule.diModule)
            bind<File>(tag = DiTag.TAG_TORRENT_DIR) with scoped(ustadTestRule.endpointScope).singleton {
                torrentClientFolder
            }
            bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(ustadTestRule.endpointScope).singleton {
                containerClientFolder
            }
            bind<UstadTorrentManager>() with scoped(ustadTestRule.endpointScope).singleton {
                UstadTorrentManagerImpl(endpoint = context, di = di)
            }
            bind<UstadCommunicationManager>() with singleton {
                UstadCommunicationManager()
            }
            bind<ContainerTorrentDownloadJob>() with scoped(ustadTestRule.endpointScope).singleton {
                ContainerTorrentDownloadJob(endpoint = context, di = di)
            }
            onReady {
                instance<UstadCommunicationManager>().start(InetAddress.getByName("0.0.0.0"))
            }
        }

    }

    @Test
    fun test(){

        val accountManager: UstadAccountManager by localDi.instance()
        accountManager.activeEndpoint = Endpoint("http://65.108.52.205:8087/")
        val containerDownloadJob: ContainerTorrentDownloadJob = localDi.on(accountManager.activeEndpoint).direct.instance()
        val seedManager: UstadTorrentManager = localDi.on(accountManager.activeEndpoint).direct.instance()
        GlobalScope.launch {
            seedManager.start()
        }

        runBlocking {
            containerDownloadJob.processJob(ContentJobItem(cjiContainerUid = 225824306785447936),
                    ProcessContext(DoorUri.parse(""), params = mutableMapOf())){
            }
        }

    }


}