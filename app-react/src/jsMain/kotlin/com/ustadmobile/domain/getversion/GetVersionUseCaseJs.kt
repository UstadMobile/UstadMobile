package com.ustadmobile.domain.getversion

import com.ustadmobile.BuildConfigJs
import com.ustadmobile.core.domain.getversion.GetVersionUseCase

class GetVersionUseCaseJs: GetVersionUseCase {

    override fun invoke(): GetVersionUseCase.VersionInfo {
        return GetVersionUseCase.VersionInfo(
            versionString = BuildConfigJs.APP_VERSION,
            buildTime = 0
        )
    }
}