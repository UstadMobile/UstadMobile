package com.ustadmobile.libcache.io

import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File
import java.io.FileOutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

actual fun Source.transferToAndGetSha256(
    path: Path,
) : TransferResult {
    val messageDigest = MessageDigest.getInstance("SHA-256")

    val bytesTransferred = DigestOutputStream(
        SystemFileSystem.sink(path).buffered().asOutputStream(),
        messageDigest
    ).use { outStream ->
        asInputStream().copyTo(outStream).also {
            outStream.flush()
        }
    }

    return TransferResult(
        sha256 = messageDigest.digest(),
        transferred = bytesTransferred,
    )
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

            val tmpFile = File(destPath.toString(), UUID.randomUUID().toString())
            DigestOutputStream(FileOutputStream(tmpFile), messageDigest).use { outStream ->
                zipInput.copyTo(outStream)
                outStream.flush()
            }

            unzippedEntries.add(
                UnzippedEntry(
                    path = Path(tmpFile.toString()),
                    name = zipEntry.name,
                    sha256 = messageDigest.digest()
                )
            )
            messageDigest.reset()
        }
    }

    return unzippedEntries
}
