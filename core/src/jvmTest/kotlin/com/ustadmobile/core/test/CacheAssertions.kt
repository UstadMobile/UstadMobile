package com.ustadmobile.core.test

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.door.util.NullOutputStream
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.io.useAndReadySha256
import com.ustadmobile.libcache.request.requestBuilder
import kotlinx.io.asInputStream
import org.junit.Assert
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

fun UstadCache.assertManifestEntryIsStored(
    manifest: ContentManifest,
    uriInManifest: String,
    originalData: () -> InputStream
) {
    val manifestEntry = manifest.entries.first {
        it.uri == uriInManifest
    }
    val cacheResponse = retrieve(requestBuilder(manifestEntry.bodyDataUrl))
    assertNotNull(cacheResponse)
    val responseSha256 = cacheResponse.bodyAsSource()!!.useAndReadySha256()
    val originalDataSha256 = originalData().use { it.readSha256() }
    assertTrue(originalDataSha256.contentEquals(responseSha256),
        message = "SHA-256 of original data and data returned for $uriInManifest " +
                "(bodyDataUrl=${manifestEntry.bodyDataUrl}")
}

fun UstadCache.assertZipIsManifested(
    manifest: ContentManifest,
    zipFile: ZipFile,
    prefix: String = ""
) {
    val entries = zipFile.entries().toList().filter { !it.isDirectory }
    entries.forEach { zipEntry ->
        assertManifestEntryIsStored(
            manifest = manifest,
            uriInManifest = "$prefix${zipEntry.name}",
            originalData = { zipFile.getInputStream(zipEntry) }
        )
    }
}
