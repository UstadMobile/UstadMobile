package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint

interface SaveLocalUrisAsBlobsUseCase {

    data class BlobToSave(
        val uid: Long,
        //The uri of the data on the device (e.g. android Uri, Java.net.URI, JS blob: uri)
        val localUri: String?
    )

    /**
     * Save a list of LocalUris as blobs.
     */
    suspend operator fun invoke(
        endpoint: Endpoint,
        tableId: Int,
        blobs: List<BlobToSave>,
        //Could add params if needed e.g. resolution etc.
    )

}