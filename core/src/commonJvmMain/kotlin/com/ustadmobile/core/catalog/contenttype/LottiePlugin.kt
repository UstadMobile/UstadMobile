package com.ustadmobile.core.catalog.contenttype

import com.google.gson.JsonElement
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.door.ext.toFile
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LottiePlugin(
    private var context: Any,
    val endpoint: Endpoint,
    override val di: DI,
    private val uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
): ContentPlugin {

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
        val jsonString = localFileUri.toFile().readText()
        val jsonElement= json.parseToJsonElement(jsonString)
        val jsonObject = jsonElement.jsonObject

        val nm: JsonElement? = jsonObject.get("nm");

        return MetadataResult(ContentEntryWithLanguage().apply {
            title = nm?.jsonPrimitive?.content
        }, PLUGIN_ID)
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        TODO("Not yet implemented")
    }

    companion object {
        const val PLUGIN_ID = 105
    }
}