package com.ustadmobile.core.domain.process

import android.app.Activity


class CloseProcessUseCaseAndroid(
    private val activity: Activity
): CloseProcessUseCase {

    override fun invoke() {
        activity.finish()
    }
}