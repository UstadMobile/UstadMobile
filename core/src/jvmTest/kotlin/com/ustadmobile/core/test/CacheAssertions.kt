package com.ustadmobile.core.test

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.door.util.NullOutputStream
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.io.useAndReadSha256
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.bodyAsUncompressedSourceIfContentEncoded
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
    val response = retrieve(iRequestBuilder(url))
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
    val response = retrieve(iRequestBuilder(url))
    val responseSha256 = response?.bodyAsSource()?.asInputStream()?.useAndDigest(digest)
        ?: throw AssertionError("assertCachedBodyMatchesZipEntry: no body for $url")
    digest.reset()

    val fileSha256 = FileInputStream(file).useAndDigest(digest)
    Assert.assertArrayEquals("Digest for $url should match ${file.absolutePath}",
        fileSha256, responseSha256)
}

fun UstadCache.assertManifestEntryIsStored(
    manifest: ContentManifest,
    uriInManifest: String,
    originalData: () -> InputStream
) {
    val manifestEntry = manifest.entries.first {
        it.uri == uriInManifest
    }
    val cacheResponse = retrieve(iRequestBuilder(manifestEntry.bodyDataUrl))
    assertNotNull(cacheResponse)
    val responseSha256 = cacheResponse.bodyAsUncompressedSourceIfContentEncoded()!!.useAndReadSha256()
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
