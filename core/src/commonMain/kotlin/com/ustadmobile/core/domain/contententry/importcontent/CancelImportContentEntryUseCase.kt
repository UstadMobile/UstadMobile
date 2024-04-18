package com.ustadmobile.core.domain.contententry.importcontent

interface CancelImportContentEntryUseCase {

    operator fun invoke(
        cjiUid: Long
    )

}