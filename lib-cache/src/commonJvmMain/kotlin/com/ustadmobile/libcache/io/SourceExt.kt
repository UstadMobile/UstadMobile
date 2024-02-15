package com.ustadmobile.libcache.io

import com.ustadmobile.libcache.CompressionType
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File
import java.io.FileOutputStream
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

actual fun Source.transferToAndGetSha256(
    path: Path,
    sourceCompression: CompressionType,
    destCompressionType: CompressionType,
) : TransferResult {
    val messageDigest = MessageDigest.getInstance("SHA-256")

    val bytesTransferred = SystemFileSystem.sink(
        path
    ).buffered().asOutputStream().compressIfRequired(destCompressionType).use { outStream ->
        DigestInputStream(
            asInputStream(), messageDigest
        ).uncompress(
            sourceCompression
        ).copyTo(outStream).also {
            outStream.flush()
        }
    }

    return TransferResult(
        sha256 = messageDigest.digest(),
        transferred = bytesTransferred,
    )
}

actual fun Source.useAndReadSha256(): ByteArray {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    asInputStream().use { inStream ->
        val buffer = ByteArray(8192)
        var bytesRead : Int
        while(inStream.read(buffer).also { bytesRead = it} != -1) {
            messageDigest.update(buffer, 0, bytesRead)
        }
    }
    return messageDigest.digest()
}

actual fun Source.unzipTo(
    destPath: Path
): List<UnzippedEntry> {
    val unzippedEntries = mutableListOf<UnzippedEntry>()
    val messageDigest = MessageDigest.getInstance("SHA-256")

    ZipInputStream(this.asInputStream()).use { zipInput ->
        lateinit var zipEntry: ZipEntry
        while(zipInput.nextEntry?.also { zipEntry = it } != null) {
            if(zipEntry.isDirectory)
                continue

            val destFile = File(destPath.toString(), zipEntry.name)
            destFile.parentFile.takeIf { !it.exists() }?.mkdirs()
            DigestOutputStream(FileOutputStream(destFile), messageDigest).use { outStream ->
                zipInput.copyTo(outStream)
                outStream.flush()
            }

            unzippedEntries.add(
                UnzippedEntry(
                    path = Path(destFile.toString()),
                    name = zipEntry.name,
                    sha256 = messageDigest.digest()
                )
            )
            messageDigest.reset()
        }
    }

    return unzippedEntries
}


actual fun Source.uncompress(
    compressionType: CompressionType
): Source {
    return if(compressionType != CompressionType.NONE) {
        asInputStream().uncompress(compressionType).asSource().buffered()
    }else {
        this
    }
}

actual fun Source.range(fromByte: Long, toByte: Long): RawSource {
    return RangeInputStream(asInputStream(), fromByte, toByte).asSource()
}
