package com.ustadmobile.core.domain.compress.list

import com.ustadmobile.libcache.io.UnzippedEntry

fun UnzippedEntry.toItemToCompress(
    mimeType: String?
): CompressListUseCase.ItemToCompress {
    return CompressListUseCase.ItemToCompress(
        path = this.path,
        name = this.name,
        mimeType = mimeType,
    )
}
