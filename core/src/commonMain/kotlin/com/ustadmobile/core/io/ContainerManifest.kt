package com.ustadmobile.core.io

import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile

/**
 * The ContainerManifest provides a list of all the contents of a given Container so that a downloader
 * can determine which files need to be downloaded, and which files the device already has.
 *
 * If a Container has the same contents, The ContainerManifest will be BINARY IDENTICAL. This is the
 * primary reason for creating this class (rather than using simple JSON serialization). Because the
 * ContainerManifest will be binary identical everytime, we can specify the expected integrity when
 * using retriever to fetch the manifest itself (and avoid any possibility of issues caused by a
 * malevolent peer device)
 *
 * Entries MUST be sorted by the entry md5
 *
 * @see com.ustadmobile.lib.db.entities.Container
 */
class ContainerManifest(val version: Int, val containerUid: Long, val entries: List<Entry>) {

    init {
        if(entries.isEmpty())
            throw IllegalArgumentException("ContainerManifest for $containerUid: empty entry list")
    }

    val headerString: String = "$FIELDNAME_VERSION:$version\n" +
        "$FIELDNAME_CONTAINERUID:$containerUid\n" +
        "$FIELDNAME_NUMENTRIES:${entries.size}\n"

    /**
     * @param originalMd5 - as per the ContainerEntryFile (Base64 encoded)
     * @param integrity as per ContainerEntryFile.cefIntegrity
     * @param crc32 as per ContainerEntryFile.cefCrc32
     * @param size as per ContainerEntryFile ceCompressedSize
     * @param inflatedSize if compressed, then the size of the entry when uncompressed
     * @param compression as per ContainerEntryFile.compression
     * @param lastModified as per ContainerEntryFile.lastModified
     */
    data class Entry(
        val pathInContainer: String,
        val originalMd5: String,
        val integrity: String,
        val crc32: Long,
        val size: Long,
        val inflatedSize: Long,
        val compression: Int,
        val lastModified: Long,
    ) {
        fun toManifestSegment() : String {
            return "$FIELDNAME_PATH_IN_CONTAINER:$pathInContainer\n" +
                    "$FIELDNAME_ORIGINAL_MD5:$originalMd5\n" +
                    "$FIELDNAME_INTEGRITY:$integrity\n" +
                    "$FIELDNAME_CRC32:$crc32\n" +
                    "$FIELDNAME_SIZE:$size\n" +
                    "$FIELDNAME_INFLATED_SIZE:$inflatedSize\n" +
                    "$FIELDNAME_COMPRESSION:$compression\n" +
                    "$FIELDNAME_LASTMODIFIED:$lastModified\n"


        }


        fun toContainerEntryWithContainerEntryFile(
            containerUid: Long
        ): ContainerEntryWithContainerEntryFile {
            return ContainerEntryWithContainerEntryFile().apply {
                ceContainerUid = containerUid
                cePath = pathInContainer
                containerEntryFile = ContainerEntryFile().apply {
                    ceCompressedSize = size
                    ceTotalSize = inflatedSize
                    cefCrc32 = crc32
                    cefMd5 = originalMd5
                    cefIntegrity = integrity
                    lastModified = this@Entry.lastModified
                    compression = this@Entry.compression
                }
            }
        }

    }

    fun toContainerEntryWithContainerEntryFileList(): List<ContainerEntryWithContainerEntryFile> {
        return entries.map {
            ContainerEntryWithContainerEntryFile().apply {
                ceContainerUid = containerUid
                cePath = it.pathInContainer
                containerEntryFile = ContainerEntryFile().apply {
                    cefIntegrity = it.integrity
                    cefMd5 = it.originalMd5
                    cefCrc32 = it.crc32
                    ceTotalSize = it.inflatedSize
                    ceCompressedSize = it.size
                    lastModified = it.lastModified
                }
            }
        }
    }

    fun toContainerEntryList(): List<ContainerEntryWithContainerEntryFile> {
        return entries.map { it.toContainerEntryWithContainerEntryFile(containerUid) }
    }

    fun toManifestString() : String {
        return headerString + "\n" + entries.joinToString(separator = "\n") { it.toManifestSegment() }
    }

    companion object {

        const val FIELDNAME_VERSION = "Version"

        const val FIELDNAME_CONTAINERUID = "ContainerUid"

        const val FIELDNAME_NUMENTRIES = "NumEntries"

        const val FIELDNAME_PATH_IN_CONTAINER = "PathInContainer"

        const val FIELDNAME_ORIGINAL_MD5 = "OriginalMD5"

        const val FIELDNAME_INTEGRITY = "Integrity"

        const val FIELDNAME_CRC32 = "CRC32"

        const val FIELDNAME_SIZE = "Size"

        const val FIELDNAME_INFLATED_SIZE = "InflatedSize"

        const val FIELDNAME_COMPRESSION = "Compression"

        const val FIELDNAME_LASTMODIFIED = "LastModified"

        fun parseFromString(str: String): ContainerManifest {
            val lineIterator = str.lineSequence().iterator()
            return parseFromLines {
                if(lineIterator.hasNext()) {
                    lineIterator.next()
                } else {
                    null
                }
            }
        }

        fun parseFromLines(lineIn: () -> String?): ContainerManifest {
            var lineCount = 0

            fun requireNextLine(): String {
                var line: String? = null
                while(lineIn().also { line = it }?.isBlank() == true) {
                    lineCount++
                }

                val lineVal = line ?: throw IllegalStateException("Manifest: Expected line at $lineCount")
                lineCount++

                return lineVal
            }

            fun requireFieldValue(requiredField: String): String {
                val line = requireNextLine()
                val colonPos = line.indexOf(":")
                val fieldName = line.substring(0, colonPos)
                if(fieldName != requiredField)
                    throw IllegalStateException("ContainerManifest: Expected $requiredField at line #$lineCount")

                return line.substring(colonPos + 1)
            }

            val version = requireFieldValue(FIELDNAME_VERSION).toInt()
            val containerUid = requireFieldValue(FIELDNAME_CONTAINERUID).toLong()
            val numEntries = requireFieldValue(FIELDNAME_NUMENTRIES).toInt()

            val entries = mutableListOf<Entry>()
            for(index in 0 until numEntries) {
                val pathInContainer = requireFieldValue(FIELDNAME_PATH_IN_CONTAINER)
                val originalMd5 = requireFieldValue(FIELDNAME_ORIGINAL_MD5)
                val integrity = requireFieldValue(FIELDNAME_INTEGRITY)
                val crc32 = requireFieldValue(FIELDNAME_CRC32).toLong()
                val size = requireFieldValue(FIELDNAME_SIZE).toLong()
                val inflatedSize = requireFieldValue(FIELDNAME_INFLATED_SIZE).toLong()
                val compression = requireFieldValue(FIELDNAME_COMPRESSION).toInt()
                val lastModified = requireFieldValue(FIELDNAME_LASTMODIFIED).toLong()

                entries += Entry(pathInContainer, originalMd5, integrity, crc32, size, inflatedSize,
                    compression, lastModified)
            }

            return ContainerManifest(version, containerUid, entries)
        }

        fun fromContainerEntryWithContainerEntryFiles(
            containerEntries: List<ContainerEntryWithContainerEntryFile>
        ) : ContainerManifest{
            val containerUid = containerEntries.firstOrNull()?.ceContainerUid
                ?: throw IllegalArgumentException("fromContainerEntryWithContainerEntryFiles: Empty!")

            return ContainerManifest(1, containerUid, containerEntries.map {
                val entryFile = it.containerEntryFile ?: throw IllegalArgumentException("Null entryFile!")

                Entry(it.cePath!!, entryFile.cefMd5!!, entryFile.cefIntegrity!!, entryFile.cefCrc32,
                    entryFile.ceCompressedSize, entryFile.ceTotalSize, entryFile.compression,
                    entryFile.lastModified)
            }.sortedBy { it.originalMd5 })
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContainerManifest) return false

        if (version != other.version) return false
        if (containerUid != other.containerUid) return false
        if (entries != other.entries) return false
        if (headerString != other.headerString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + containerUid.hashCode()
        result = 31 * result + entries.hashCode()
        result = 31 * result + headerString.hashCode()
        return result
    }


}