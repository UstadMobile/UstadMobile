package com.ustadmobile.core.domain.blob.openblob

interface OpenBlobUseCase {

    suspend operator fun invoke(item: OpenBlobItem)

}