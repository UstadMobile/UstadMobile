package com.ustadmobile.core.getdeveloperinfo

import com.ustadmobile.core.domain.getdeveloperinfo.GetDeveloperInfoUseCase
import java.io.File

class GetDeveloperInfoUseCaseJvm(
    private val appResourcesDir: File,
    private val dataDir: File,
): GetDeveloperInfoUseCase {
    override fun invoke(): GetDeveloperInfoUseCase.DeveloperInfo {
        return GetDeveloperInfoUseCase.DeveloperInfo(
            infoMap = mapOf(
                "App resources dir" to appResourcesDir.absolutePath,
                "Data dir" to dataDir.absolutePath,
            )
        )
    }
}