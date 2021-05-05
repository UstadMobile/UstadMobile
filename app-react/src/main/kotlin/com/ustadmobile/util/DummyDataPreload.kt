package com.ustadmobile.util

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

data class UmDummy(var title: String, val link:String = "", val uid: Long = 0L, val leaf: Boolean = true,
                   val parent: String = UstadView.MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                   val thumb: String = ""
)

class DummyDataPreload(private val endPoint:String, private val di: DI) {

    private val httpClient: HttpClient by di.instance()

    val systemImpl : UstadMobileSystemImpl by di.instance()

    val entries = mutableListOf(
        UmDummy("Адабиёти бадеии бачагона/Бо забони русӣ", uid = 2003300L, leaf = false),
        UmDummy("Теремок", uid = 2003301L, parent = "2003300",
             link= "https://maktabmobile.org/librera/Librera.Cloud/Адабиёти бадеии бачагона/Бо забони русӣ/Теремок.epub",
            thumb = "https://docs.google.com/uc?export=download&id=1Va0RixPFmV-CB2TttahexAcT35lP52H7"),
        UmDummy("Кори савоб", uid = 2003302L,
            link= "https://maktabmobile.org/librera/Librera.Cloud/Адабиёти бадеии бачагона/Бо забони тоҷикӣ/Кори савоб.epub",
            thumb = "https://docs.google.com/uc?export=download&id=1_k55vT2pYMfx7Z8FYnagr5to731afT0A"),
        UmDummy("Образованный человек и обманщик", uid = 2003303L,
            link= "https://maktabmobile.org/librera/Librera.Cloud/Адабиёти бадеии бачагона/Бо забони русӣ/Образованный человек и обманщик.epub"),
        UmDummy("Рекомендации для родителей", uid = 2003304L,
            link= "https://maktabmobile.org/librera/Librera.Cloud/Дастуру тавсияҳо барои падару модарон/Рекомендации для родителей.epub",
            thumb = "https://docs.google.com/uc?export=download&id=1FFETm2OsWbyMhUzgTYgS8xCEXj6p7VHE"),
        UmDummy("Лавҳаҳо оид ба муҳиммияти хониш ба се забон̆", uid = 2003305L,
            link= "https://maktabmobile.org/librera/Librera.Cloud/Дастуру тавсияҳо барои падару модарон/Лавҳаҳо оид ба муҳиммияти хониш ба се забон.epub",
            thumb = "https://docs.google.com/uc?export=download&id=1JRCHlDVZavcU8u2clmsn8R_aeTRlQjLS"),
        UmDummy("Дервиш и муравей̆", uid = 2003306L, parent = "2003300",
            link= "https://maktabmobile.org/librera/Librera.Cloud/Адабиёти бадеии бачагона/Бо забони русӣ/Дервиш и муравей.epub"),
        UmDummy("Как кот зверей напугал", uid = 2003307L, parent = "2003300",
            link= "https://maktabmobile.org/librera/Librera.Cloud/Адабиёти бадеии бачагона/Бо забони русӣ/Как кот зверей напугал.epub")
    )

    private val availableEntryMetaData = mutableListOf<ImportedContentEntryMetaData>()

    private val availableContainers = mutableListOf<Container>()

    private lateinit var onTaskFinished:()-> Unit

    suspend fun verifyAndImportEntries(onFinished: () -> Unit){
        this.onTaskFinished = onFinished
        validateLinks(0){
            val containers = getAllContainers()
            if(containers.isNotEmpty() && entries.filter { it.leaf }.size == containers.size) {
                availableContainers.addAll(containers)
                saveContentOnLocalStorage()
            }else mountContainer(0){
                val mountedContainers = getAllContainers()
                availableContainers.addAll(mountedContainers)
                saveContentOnLocalStorage()
            }

        }
    }

    private suspend fun validateLinks(nextIndex: Int, onDone: suspend () -> Unit){
        if(entries.size == nextIndex){
            onDone()
            return
        }
        val entry = entries[nextIndex]
        if(entry.leaf){
            httpClient.post<HttpStatement> {
                url(UMFileUtil.joinPaths(endPoint, "/import/validateLink"))
                parameter("url", entry.link)
                expectSuccess = false
            }.execute{
                val metaData = it.receive<ImportedContentEntryMetaData>()
                metaData.contentEntry.apply {
                    title = entry.title
                    entryId = entry.parent
                    contentEntryUid = entry.uid
                    thumbnailUrl = entry.thumb
                }
                availableEntryMetaData.add(metaData)
                validateLinks(nextIndex + 1, onDone)

            }
        }else {
            val entryWithLanguage = ContentEntryWithLanguage().apply {
                title = entry.title
                entryId = entry.parent
                contentEntryUid = entry.uid
                thumbnailUrl = entry.thumb
            }
            availableEntryMetaData.add(ImportedContentEntryMetaData(entryWithLanguage,"",""))
            validateLinks(nextIndex.plus(1), onDone)
        }
    }

    private suspend fun mountContainer(nextIndex: Int, onDone: suspend () -> Unit){
        val expectedContainers = availableEntryMetaData.filter {it.contentEntry.leaf}
        if(nextIndex == expectedContainers.size){
            onDone()
            return
        }
        val data = expectedContainers[nextIndex]
        httpClient.post<HttpStatement> {
            url(UMFileUtil.joinPaths(endPoint, "/import/downloadLink"))
            parameter("parentUid", data.contentEntry.entryId)
            parameter("scraperType", data.scraperType)
            parameter("url", data.uri)
            parameter("conversionParams",
                Json.encodeToString(MapSerializer(String.serializer(),
                    String.serializer()),
                    mapOf("compress" to "true", "dimensions" to "0x0")))
            header("content-type", "application/json")
            body = data.contentEntry
        }.execute{
            if(it.status == HttpStatusCode.OK){
                mountContainer(nextIndex.plus(1), onDone)
            }else{
                console.log("Something went wrong, check server data")
            }
        }
    }


    private suspend fun getAllContainers(): List<Container>{
        val client = httpClient.get<HttpStatement> {
            url(UMFileUtil.joinPaths(endPoint, "/ContainerMount/recentContainers"))
            parameter("uids", entries.filter { it.leaf}.map { it.uid}.joinToString(","))
            header("content-type", "application/json")
        }.execute()
        return client.receive()
    }

    private fun saveContentOnLocalStorage(){
        systemImpl.setAppPref(TAG_ENTRIES, safeStringify(di,
            ListSerializer(ContentEntryWithLanguage.serializer()),availableEntryMetaData.map { it.contentEntry }), this)
        systemImpl.setAppPref(
            TAG_CONTAINERS, safeStringify(di, ListSerializer(Container.serializer()),
                availableContainers), this)
        onTaskFinished()
    }

    companion object {
        const val TAG_ENTRIES = "store_entries"
        const val TAG_CONTAINERS = "store_containers"
    }
}