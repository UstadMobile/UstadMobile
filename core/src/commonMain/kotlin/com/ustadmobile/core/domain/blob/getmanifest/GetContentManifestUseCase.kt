package com.ustadmobile.core.domain.blob.getmanifest

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

class GetContentManifestUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val httpClient: HttpClient,
    private val json: Json,
) {

    data class GetContentManifestResult(
        val manifest: ContentManifest,
        val manifestContentLength: Long,
        val contentEntryVersion: ContentEntryVersion,
    )

    suspend operator fun invoke(
        contentEntryVersionUid: Long
    ): GetContentManifestResult {
        val contentEntryVersion = db.contentEntryVersionDao()
            .findByUidAsync(contentEntryVersionUid)
                ?: repo?.contentEntryVersionDao()?.findByUidAsync(contentEntryVersionUid)
                ?: throw IllegalArgumentException("ContentEntryVersion $contentEntryVersionUid not in db/repo")

        val manifestUrl = contentEntryVersion.cevManifestUrl!!
        val manifestResponse = httpClient.get(manifestUrl)
        val manifestSize = manifestResponse.headers["content-length"]?.toLong() ?: 0

        return GetContentManifestResult(
            manifest = json.decodeFromString(manifestResponse.bodyAsDecodedText()),
            manifestContentLength = manifestSize,
            contentEntryVersion = contentEntryVersion,
        )

    }
}