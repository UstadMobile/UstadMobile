package com.ustadmobile.core.domain.contententry.importcontent

import android.content.Context
import androidx.work.WorkManager
import com.ustadmobile.core.account.LearningSpace

class CancelImportContentEntryUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: LearningSpace,
): CancelImportContentEntryUseCase {

    override fun invoke(cjiUid: Long) {
        WorkManager.getInstance(appContext).cancelUniqueWork(
            EnqueueContentEntryImportUseCase.uniqueNameFor(endpoint, cjiUid)
        )
    }
}