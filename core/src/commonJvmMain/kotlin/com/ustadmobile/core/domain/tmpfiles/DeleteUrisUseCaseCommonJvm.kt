package com.ustadmobile.core.domain.tmpfiles

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import io.github.aakira.napier.Napier

class DeleteUrisUseCaseCommonJvm(
    private val isTempFileCheckerUseCase: IsTempFileCheckerUseCase,
) : DeleteUrisUseCase {

    override suspend fun invoke(uris: List<String>, onlyIfTemp: Boolean) {
        uris.forEach {
            if(!onlyIfTemp || isTempFileCheckerUseCase(it)) {
                Napier.d { "DeleteUrisUseCase: delete $it" }
                DoorUri.parse(it).toFile().delete()
            }
        }
    }
}