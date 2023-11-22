package com.ustadmobile.core.test

import com.ustadmobile.door.util.NullOutputStream
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import kotlinx.io.asInputStream
import org.junit.Assert
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.ZipFile

fun InputStream.useAndDigest(digest: MessageDigest): ByteArray {
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
    val responseSha256 = response?.bodyAsSource()?.asInputStream()?.useAndDigest(digest)
        ?: throw AssertionError("assertCachedBodyMatchesZipEntry: no body for $url")
    digest.reset()

    val zipEntry = zipFile.getEntry(pathInZip)
    val zipEntryDigest = zipFile.getInputStream(zipEntry).useAndDigest(digest)
    Assert.assertArrayEquals("Digest for $url should match entry in zip $zipFile!$pathInZip",
        zipEntryDigest, responseSha256)
}

fun UstadCache.assertCachedBodyMatchesFileContent(
    url: String,
    file: File
) {
    val digest = MessageDigest.getInstance("SHA-256")
    val response = retrieve(requestBuilder(url))
    val responseSha256 = response?.bodyAsSource()?.asInputStream()?.useAndDigest(digest)
        ?: throw AssertionError("assertCachedBodyMatchesZipEntry: no body for $url")
    digest.reset()

    val fileSha256 = FileInputStream(file).useAndDigest(digest)
    Assert.assertArrayEquals("Digest for $url should match ${file.absolutePath}",
        fileSha256, responseSha256)
}

/**
 * Assert that the cache has a corresponding entry for each entry in the given zip
 * in the form of urlPrefix/entryPath for all entries in the zip.
 *
 * @param urlPrefix the url prefix to lookup
 * @param zip the ZipFile to check against
 */
fun UstadCache.assertZipIsCached(
    urlPrefix: String,
    zip: ZipFile,
) {
    if(!urlPrefix.endsWith("/"))
        throw IllegalArgumentException("URL prefix must end with /")

    val entries = zip.entries().toList()
    entries.filter { !it.isDirectory }.forEach {
        assertCachedBodyMatchesZipEntry(
            url = "$urlPrefix${it.name}",
            zipFile = zip,
            pathInZip = it.name
        )
    }
}
