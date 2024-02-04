package com.ustadmobile.core.domain

import com.jcabi.manifests.Manifests
import com.ustadmobile.core.domain.getversion.GetVersionUseCase

class GetVersionUseCaseJvm: GetVersionUseCase {

    private val versionProp: String by lazy {
        Manifests.read("Ustad-Version")
    }

    override fun invoke(): GetVersionUseCase.VersionInfo {
        return GetVersionUseCase.VersionInfo(versionProp, 0)
    }
}