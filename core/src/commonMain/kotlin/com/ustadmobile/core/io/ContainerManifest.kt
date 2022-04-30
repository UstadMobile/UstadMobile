package com.ustadmobile.core.io

import com.ustadmobile.lib.db.entities.ContainerEntryWithChecksums

/**
 * The ContainerManifest provides a list of all the contents of a given Container so that a downloader
 * can determine which files need to be downloaded, and which files the device already has.
 *
 * The ContainerManifest will be BINARY IDENTICAL everytime. This is the primary reason for creating
 * this class (rather than using simple JSON serialization).
 *
 * Entries MUST be sorted by the entry md5
 *
 * This is important so that retriever
 * itself can retrieve the manifest (securely by checking a known integrity checksum).
 *
 * @see com.ustadmobile.lib.db.entities.Container
 */
class ContainerManifest(val version: Int, val containerUid: Long, val entries: List<Entry>) {

    val headerString: String = "$FIELDNAME_VERSION:$version\n" +
        "$FIELDNAME_CONTAINERUID:$containerUid\n" +
        "$FIELDNAME_NUMENTRIES:${entries.size}\n"

    /**
     * @param originalMd5 - as per the ContainerEntryFile (Base64 encoded)
     * @param integrity as per ContainerEntryFile
     * @param size as per ContainerEntryFile ceCompressedSize
     */
    data class Entry(
        val pathInContainer: String,
        val originalMd5: String,
        val integrity: String,
        val size: Long
    ) {
        fun toManifestSegment() : String {
            return "$FIELDNAME_PATH_IN_CONTAINER:$pathInContainer\n" +
                    "$FIELDNAME_ORIGINAL_MD5:$originalMd5\n" +
                    "$FIELDNAME_INTEGRITY:$integrity\n" +
                    "${FIELDNAME_SIZE}:$size\n"
        }
    }

    fun toManifestString() : String{
        return headerString + "\n" + entries.joinToString(separator = "\n") { it.toManifestSegment() }
    }

    companion object {

        const val FIELDNAME_VERSION = "Version"

        const val FIELDNAME_CONTAINERUID = "ContainerUid"

        const val FIELDNAME_NUMENTRIES = "NumEntries"

        const val FIELDNAME_PATH_IN_CONTAINER = "PathInContainer"

        const val FIELDNAME_ORIGINAL_MD5 = "OriginalMD5"

        const val FIELDNAME_INTEGRITY = "Integrity"

        const val FIELDNAME_SIZE = "Size"

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
                val size = requireFieldValue(FIELDNAME_SIZE).toLong()
                entries += Entry(pathInContainer, originalMd5, integrity, size)
            }

            return ContainerManifest(version, containerUid, entries)
        }

        /**
         * Generate a Manifest given a list of ContainerEntryWithChecksums
         */
        fun fromContainerEntryWithChecksums(
            containerEntries: List<ContainerEntryWithChecksums>
        ): ContainerManifest {
            return ContainerManifest(1, containerEntries.first().ceContainerUid,
                containerEntries.sortedBy { it.cefMd5 }.map {
                    Entry(it.cePath!!, it.cefMd5!!, it.cefIntegrity!!, it.ceCompressedSize)
                })
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