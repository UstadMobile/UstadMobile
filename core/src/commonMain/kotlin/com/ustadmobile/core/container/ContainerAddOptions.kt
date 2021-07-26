package com.ustadmobile.core.container

import com.ustadmobile.door.DoorUri

class ContainerAddOptions(val storageDirUri: DoorUri,

                          val moveFiles: Boolean = false,
                          /**
                           * Controls which entries get compressed or not.
                           */
                          val compressionFilter: CompressionFilter = DEFAULT_COMPRESSION_FILTER,

                          /**
                           * Controls the path that added entries will get inside the container.
                           */
                          val fileNamer: ContainerFileNamer = DEFAULT_FILE_NAMER,

                          /**
                           * Controls whether or not to update the properties (e.g. total size etc)
                           * of the container itself. This should be true when the container is being
                           * modified (e.g. when something is imported), but false when the container
                           * is being downloaded.
                           */
                          val updateContainer: Boolean = true

                          //TODO: add progresslistener
) {


    companion object {

        val DEFAULT_COMPRESSION_FILTER = object: CompressionFilter {
            private val mediaExtensions = listOf(".mp4", ".mkv", ".webm", ".mov", ".avi", ".flv",
                ".mp3", ".ogg", ".wav", ".au", ".3gp", ".m4a", ".m4v")

            override fun shouldCompress(pathInContainer: String, mimeType: String?) : Boolean{
                val isVideoExtension = pathInContainer.toLowerCase().let { pathLowerCase ->
                    mediaExtensions.any { pathLowerCase.endsWith(it) }
                }
                return !isVideoExtension && mimeType?.startsWith("video/") != true
            }
        }

        val DEFAULT_FILE_NAMER = object: ContainerFileNamer {
            override fun nameContainerFile(relPathIn: String, uriIn: String) = relPathIn
        }

    }

}