package com.ustadmobile.core.domain.saveblob

import com.ustadmobile.core.account.Endpoint

interface SaveBlobUseCase {

    data class BlobToSave(
        val tableId: Int,
        val uid: Long,
        //The uri of the data on the device (e.g. android Uri, Java.net.URI, JS blob: uri)
        val localUri: String
    )

    /**
     * Could be :
     *    PersonPicture(
     *    ContentEntryVersionFile(pathInContent, uri)
     *
     *
     * Process (Android/JVM):
     *    a) Copy each item from the localUri into the cache as https://endpoint.com/api/blob/sha256/content-type
     *    b) Invoke the adapter's updateBlobUri to set the entity url locally to the cache url
     *    c) Upload to the server (use OKHttp or KTOR)
     *    d) Invoke adapter replicateUpstream
     */
    suspend operator fun invoke(
        endpoint: Endpoint,
        blobs: List<BlobToSave>,
        //Could add params if needed e.g. resolution etc.
    )

}