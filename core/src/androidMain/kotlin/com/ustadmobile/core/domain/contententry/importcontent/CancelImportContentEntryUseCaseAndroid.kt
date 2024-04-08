package com.ustadmobile.core.domain.contententry.importcontent

import android.content.Context
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint

class CancelImportContentEntryUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: Endpoint,
): CancelImportContentEntryUseCase {

    override fun invoke(cjiUid: Long) {
        WorkManager.getInstance(appContext).cancelUniqueWork(
            EnqueueContentEntryImportUseCase.uniqueNameFor(endpoint, cjiUid)
        )
    }
}