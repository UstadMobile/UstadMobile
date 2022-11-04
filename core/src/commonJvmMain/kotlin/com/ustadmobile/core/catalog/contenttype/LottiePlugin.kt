package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.lib.db.entities.Container
import org.kodein.di.on

class LottiePlugin(
    context: Any,
    endpoint: Endpoint,
    override val di: DI,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
): ContentImportContentPlugin(endpoint, context, uploader) {

    override val pluginId: Int = PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = listOf("application/json")

    override val supportedFileExtensions: List<String>
        get() = listOf("json")

    override suspend fun extractMetadata(
        uri: DoorUri, // could be file:/// or could be http://
        process: ContentJobProcessContext
    ): MetadataResult {
        val json: Json = di.direct.instance()
        val localFileUri = process.getLocalOrCachedUri()
        val jsonString = localFileUri.openInputStream(context)?.readString()
            ?: throw IllegalArgumentException("cannot open inputstream!")
        val jsonElement= json.parseToJsonElement(jsonString)
        val jsonObject = jsonElement.jsonObject

        val nm: JsonElement? = jsonObject.get("nm")

        return MetadataResult(ContentEntryWithLanguage().apply {
            title = nm?.jsonPrimitive?.content
            leaf = true
        }, PLUGIN_ID)
    }

    override suspend fun makeContainer(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progressListener: ContentJobProgressListener,
        containerStorageUri: DoorUri,
    ): Container {
        val repo: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

        val mimeType = supportedMimeTypes.first()

        return repo.containerBuilder(jobItem.contentJobItem?.cjiContentEntryUid ?: 0,
            supportedMimeTypes.first(), containerStorageUri)
            .addUri("lottie.json", process.getLocalOrCachedUri(), context)
            .build()
    }

//    override suspend fun processJob(
//        jobItem: ContentJobItemAndContentJob,
//        process: ContentJobProcessContext,
//        progress: ContentJobProgressListener
//    ): ProcessResult {
//        TODO("Not yet implemented")
//    }

    companion object {
        const val PLUGIN_ID = 105
    }
}