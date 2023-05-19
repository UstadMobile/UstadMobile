package com.ustadmobile.port.android.impl.nav

import java.io.File

interface NavHostTempFileRegistrar {

    /**
     * Register a temporary file as being associated with the current destination. The file will
     * be stored in the cache and deleted when the destination is popped off the back stack.
     *
     * see NavControllerExt.registerDestinationTempFile
     *
     * @param file Temporary file to register
     * @param name name of the file e.g. photo
     */
    fun registerNavDestinationTemporaryFile(file: File)

}