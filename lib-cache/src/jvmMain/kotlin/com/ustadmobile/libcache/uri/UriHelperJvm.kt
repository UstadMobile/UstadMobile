package com.ustadmobile.libcache.uri

import com.ustadmobile.libcache.headers.MimeTypeHelper
import java.io.FileInputStream
import java.io.InputStream
import kotlin.io.path.toPath

class UriHelperJvm(
    private val mimeTypeHelper: MimeTypeHelper,
): IUriHelper {

    override fun contentLength(uri: IUri): Long {
        return (uri as UriJvm).uri.toPath().toFile().length()
    }

    override fun openInputStream(uri: IUri): InputStream {
        val file = (uri as UriJvm).uri.toPath().toFile()
        return FileInputStream(file)
    }

    override fun mimeType(uri: IUri): String? {
        return mimeTypeHelper.mimeTypeByUri(uri.toString())
    }

}