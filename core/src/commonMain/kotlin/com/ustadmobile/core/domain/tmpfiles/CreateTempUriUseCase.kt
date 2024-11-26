package com.ustadmobile.core.domain.tmpfiles

import com.ustadmobile.door.DoorUri

/**
 * Interface to create a writable temporary uri (implemented by a file) on supported platforms.
 *
 * If we use File.createTemp we don't get to control where the file is stored
 */
interface CreateTempUriUseCase {

    suspend operator fun invoke(
        prefix: String,
        postfix: String,
    ): DoorUri

}