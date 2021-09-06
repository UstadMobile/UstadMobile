package com.ustadmobile.core.torrent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
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
import java.net.URL

class TestTorrentClient {

    private lateinit var localDi: DI

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    //@Before
    fun setup() {

        localDi = DI {
            import(ustadTestRule.diModule)
            bind<ContainerTorrentDownloadJob>() with scoped(ustadTestRule.endpointScope).singleton {
                ContainerTorrentDownloadJob(endpoint = context, di = di)
            }
            val trackerUrl = URL("http://127.0.0.1:8000/announce")
            bind<UstadTorrentManager>() with scoped(ustadTestRule.endpointScope).singleton {
                UstadTorrentManagerImpl(endpoint = context, di = di)
            }
            bind<UstadCommunicationManager>() with singleton {
                UstadCommunicationManager()
            }
            onReady {
                instance<UstadCommunicationManager>().start(InetAddress.getByName(trackerUrl.host))
                GlobalScope.launch {
                    val ustadTorrentManager: UstadTorrentManager = di.on(Endpoint("localhost")).direct.instance()
                    ustadTorrentManager.start()

                }
            }
        }

    }

    //@Test
    fun test(){

        val accountManager: UstadAccountManager by localDi.instance()
        accountManager.activeEndpoint = Endpoint("http://65.108.52.205:8087/")
        val containerDownloadJob: ContainerTorrentDownloadJob = localDi.on(accountManager.activeEndpoint).direct.instance()
        val seedManager: UstadTorrentManager = localDi.on(accountManager.activeEndpoint).direct.instance()
        GlobalScope.launch {
            seedManager.start()
        }

        runBlocking {
            containerDownloadJob.processJob(ContentJobItemAndContentJob().apply{
                contentJobItem = ContentJobItem(cjiContainerUid = 225824306785447936)
            },
                ProcessContext(DoorUri.parse(""), params = mutableMapOf())){
            }
        }

    }


}