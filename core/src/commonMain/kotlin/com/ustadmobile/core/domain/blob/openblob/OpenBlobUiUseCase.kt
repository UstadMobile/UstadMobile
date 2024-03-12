package com.ustadmobile.core.domain.blob.openblob

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import io.github.aakira.napier.Napier

/**
 * Runs the OpenBlob use case, and updates a UI. Handles try/catch and showing error if required
 */
class OpenBlobUiUseCase(
    val openBlobUseCase: OpenBlobUseCase,
    private val systemImpl: UstadMobileSystemImpl,
) {

    suspend operator fun invoke(
        openItem: OpenBlobItem,
        onUiUpdate: (OpeningBlobState?) -> Unit,
        intent: OpenBlobUseCase.OpenBlobIntent = OpenBlobUseCase.OpenBlobIntent.VIEW,
    ) {
        onUiUpdate(
            OpeningBlobState(
                item = openItem,
                bytesReady = 0,
                totalBytes = 0,
            )
        )

        try {
            openBlobUseCase(
                item = openItem,
                onProgress = { transferred, total ->
                    onUiUpdate(
                        OpeningBlobState(
                            item = openItem,
                            bytesReady = transferred,
                            totalBytes = total,
                        )
                    )
                },
                intent = intent,
            )

            onUiUpdate(null)
        }catch(e: Throwable) {
            Napier.w("ClazzAssignmentDetailOverview: could not open $openItem", e)
            onUiUpdate(
                OpeningBlobState(
                    item = openItem,
                    bytesReady = 0,
                    totalBytes = 0,
                    error = e.message ?: systemImpl.getString(MR.strings.error)
                )
            )
        }
    }

}