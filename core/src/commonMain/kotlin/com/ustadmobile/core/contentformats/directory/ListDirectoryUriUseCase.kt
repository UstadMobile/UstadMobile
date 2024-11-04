package com.ustadmobile.core.contentformats.directory

import com.ustadmobile.door.DoorUri

interface ListDirectoryUriUseCase {
    data class ListDirectoryItem(val uri: DoorUri, val fileName: String)

    /**
     * Lists the contents of a directory URI.
     * @param directoryUri URI as a string of the directory to list.
     * @return List of DoorUri instances representing files in the directory.
     * @throws InvalidDirectoryUriException if the provided URI is not a valid directory.
     */
    @Throws(InvalidDirectoryUriException::class)
    operator fun invoke(directoryUri: String): List<ListDirectoryItem>

    /**
     * Checks if a URI represents a directory.
     * @param directoryUri URI as a string to check.
     * @return true if the URI is a directory, false otherwise.
     */
    fun isDirectory(directoryUri: String): Boolean

}

class InvalidDirectoryUriException(message: String) : Exception(message)
