package com.ustadmobile.core.test

import com.ustadmobile.door.util.NullOutputStream
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import kotlinx.io.asInputStream
import org.junit.Assert
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.ZipFile

fun InputStream.digest(digest: MessageDigest): ByteArray {
    return DigestInputStream(this, digest).use {
        copyTo(NullOutputStream())
        digest.digest()
    }
}

fun UstadCache.assertCachedBodyMatchesZipEntry(
    url: String,
    zipFile: ZipFile,
    pathInZip: String,
) {
    val digest = MessageDigest.getInstance("SHA-256")
    val response = retrieve(requestBuilder(url))
    val responseSha256 = response!!.bodyAsSource()!!.asInputStream().digest(digest)
    digest.reset()

    val zipEntry = zipFile.getEntry(pathInZip)
    val zipEntryDigest = zipFile.getInputStream(zipEntry).digest(digest)
    Assert.assertArrayEquals("Digest for $url should match entry in zip $zipFile!$pathInZip",
        zipEntryDigest, responseSha256)
}
