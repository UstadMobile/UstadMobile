package com.ustadmobile.core.util.ext

import com.ustadmobile.core.io.ext.FILE_EXTENSION_CE_JSON
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import java.io.File
import java.io.FileFilter
import kotlinx.serialization.json.Json

/**
 * Shorthand
 */
internal fun File.listContentEntryJsonFiles() : Array<File> = listFiles(FileFilter {
    it.isFile && it.name.endsWith(FILE_EXTENSION_CE_JSON)
}) ?: arrayOf()

internal fun File.getContentEntryJsonFilesFromDir(json: Json) = listContentEntryJsonFiles().map {
    json.decodeFromString(ContainerEntryFile.serializer(), it.readText())
}

internal fun File.deleteAllContentEntryJsonFiles() = listContentEntryJsonFiles()?.forEach {
    it.delete()
}

@Deprecated("Now using withChecksum - this should be removable")
internal fun List<ContainerEntryWithMd5>.filterNotInDirectory(dir: File): List<ContainerEntryWithMd5> {
    return filter { !File(dir, it.cefMd5?.base64EncodedToHexString() ?: ".").exists() }
}

/**
 * Filter out ContainerEntryWithContainerEntryFile for which there is a .ce.json
 * file in the given directory (e.g. this file has already been downloaded)
 */
internal fun List<ContainerEntryWithContainerEntryFile>.filterContainerEntryNotInDirectory(
    dir: File
) : List<ContainerEntryWithContainerEntryFile> {
    return filter {
        val ceJsonFileName = it.containerEntryFile?.cefMd5?.base64EncodedToHexString() +
            FILE_EXTENSION_CE_JSON
        !File(dir, ceJsonFileName ?: ".").exists()
    }
}


