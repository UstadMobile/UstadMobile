package com.ustadmobile.core.domain.getdeveloperinfo

import android.content.Context

class GetDeveloperInfoUseCaseAndroid(
    private val appContext: Context,
): GetDeveloperInfoUseCase {
    override fun invoke(): GetDeveloperInfoUseCase.DeveloperInfo {
        return GetDeveloperInfoUseCase.DeveloperInfo(
            buildMap {
                this["App Files Directory"] = appContext.filesDir.toString()
            }
        )
    }
}