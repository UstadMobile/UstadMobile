package com.ustadmobile.core.domain.compress.pdf

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.util.ext.waitForAsync
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import java.io.File
import java.util.UUID

/**
 * Compress a PDF using Ghostscript command. Java PDF tools (e.g. Itext, PDFBox) produced incorrect
 * output (e.g. graphics were mangled - had black background, etc).
 *
 * Compress Pdf Use Case will only be used when the GhostScript command is detected.
 */
class CompressPdfUseCaseJvm(
    private val gsPath: File,
    private val workDir: File,
) : CompressPdfUseCase {


    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? = withContext(Dispatchers.IO) {
        val inFile = DoorUri.parse(fromUri).toFile()

        val destFile = if(toUri != null) {
            DoorUri.parse(toUri).toFile()
        }else {
            File(workDir, UUID.randomUUID().toString())
        }

        val cmd = listOf(
            gsPath.absolutePath, "-sDEVICE=pdfwrite", "-dCompatibilityLevel=1.4",
            "-dPDFSETTINGS=/ebook", "-dNOPAUSE", "-dBATCH",
            "-sOutputFile=${destFile.absolutePath}",
            inFile.absolutePath
        )

        Napier.d { "CompressPdfUseCaseJvm: running ${cmd.joinToString(separator = " ")} " }

        val process = ProcessBuilder(cmd)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .directory(workDir)
            .start()

        val numPages = Loader.loadPDF(inFile).numberOfPages
        val fileSizeIn = inFile.length()

        val outputReaderJob = launch {
            process.inputStream.bufferedReader().use { reader ->
                do {
                    val line: String? = reader.readLine()
                    if(line != null && line.startsWith("Page", ignoreCase = true)) {
                        val pageNumCompleted = line.split(" ").lastOrNull()?.toIntOrNull()
                        if(pageNumCompleted != null && pageNumCompleted > 0) {
                            Napier.v { "CompressPdfUseCaseJvm: completed page $pageNumCompleted" }
                            onProgress?.invoke(
                                CompressProgressUpdate(
                                    fromUri = fromUri,
                                    completed = ((fileSizeIn.toFloat() / numPages) * pageNumCompleted).toLong(),
                                    total = fileSizeIn
                                )
                            )
                        }
                    }
                }while(isActive && line != null)
            }
        }

        val exitValue = process.waitForAsync()
        outputReaderJob.cancel()
        onProgress?.invoke(
            CompressProgressUpdate(
                fromUri = fromUri,
                completed = inFile.length(),
                total = inFile.length(),
            )
        )

        val compressedSize = destFile.length()
        if(exitValue == 0 && compressedSize < fileSizeIn) {
            Napier.d {
                "CompressPdfUseCaseJvm: compressed $fromUri from " +
                        "${inFile.length()} bytes to ${destFile.length()} bytes"
            }

            CompressResult(
                uri = destFile.toDoorUri().toString(),
                mimeType = "application/pdf",
                compressedSize = compressedSize,
                originalSize = fileSizeIn,
            )
        }else {
            Napier.d {
                "CompressPdfUseCaseJvm: compressed file is bigger or non-zero exit code ($exitValue)! " +
                        "$fromUri from ${inFile.length()} bytes to ${destFile.length()} bytes"
            }
            destFile.delete()
            null
        }

    }
}