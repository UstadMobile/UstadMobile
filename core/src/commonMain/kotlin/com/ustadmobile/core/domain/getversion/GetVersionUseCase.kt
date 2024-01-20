package com.ustadmobile.core.domain.getversion

interface GetVersionUseCase {

    data class VersionInfo(
        val versionString: String,
        val buildTime: Long,
    )

    operator fun invoke(): VersionInfo

}