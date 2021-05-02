package com.ustadmobile.util

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import redux.RAction

data class UmDummy(var title: String, val link:String, val parent: Long = UstadView.MASTER_SERVER_ROOT_ENTRY_UID)

data class DummyStore(var entries: MutableList<ContentEntryWithLanguage> = mutableListOf(),
                      var containers: MutableList<Container> = mutableListOf()): RAction

class DummyDataPreload(private val endPoint:String, val di: DI) {

    private val httpClient: HttpClient by di.instance()

    var startUid = 2003301L

    val entries = mutableListOf(
        UmDummy("Теремок","https://docs.google.com/uc?export=download&id=1ho_uGKmfViSCM9gnbDp7GWdXK3d0HEaA"),
        UmDummy("Теремок","https://docs.google.com/uc?export=download&id=1ho_uGKmfViSCM9gnbDp7GWdXK3d0HEaA")
    )

    private val availableEntries = mutableListOf<ContentEntryWithLanguage>()

    private val availableContainers = mutableListOf<Container>()

    suspend fun verifyAndImportEntries(onFinished:()-> Unit){
        entries.forEach {entry ->
            httpClient.post<HttpStatement> {
                url(UMFileUtil.joinPaths(endPoint, "/import/validateLink"))
                parameter("url", entry.link)
                expectSuccess = false
            }.execute{ it ->
                val metaData = it.receive<ImportedContentEntryMetaData>()
                startUid+=1
                metaData.contentEntry.apply {
                    title = entry.title
                    contentEntryUid = startUid
                }
                httpClient.post<HttpStatement> {
                    url(UMFileUtil.joinPaths(endPoint, "/import/downloadLink"))
                    parameter("parentUid", entry.parent)
                    parameter("scraperType", metaData.scraperType)
                    parameter("url", metaData.uri)
                    parameter("conversionParams",
                        Json.encodeToString(MapSerializer(String.serializer(),
                            String.serializer()),
                            mapOf("compress" to "true", "dimensions" to "dimensions")))
                    header("content-type", "application/json")
                    body = metaData.contentEntry
                }.execute{response ->
                    if(response.status.value == 200){
                        availableEntries.add(metaData.contentEntry)
                        if(availableEntries.size == entries.size){
                            httpClient.get<HttpStatement> {
                                url(UMFileUtil.joinPaths(endPoint, "/ContainerMount/recentContainers"))
                                parameter("uids", availableEntries.map { it.contentEntryUid}.joinToString(","))
                                header("content-type", "application/json")
                            }.execute{
                                availableContainers.addAll(it.receive<List<Container>>())
                                console.log(availableContainers, availableEntries)
                                StateManager.dispatch(DummyStore(entries = availableEntries, containers = availableContainers))
                                onFinished()
                            }
                        }
                    }
                }
                return@execute
            }
        }
    }
}