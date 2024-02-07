package com.ustadmobile.core.domain.getdeveloperinfo

interface GetDeveloperInfoUseCase {

    data class DeveloperInfo(
        val infoMap: Map<String, String>
    )

    operator fun invoke(): DeveloperInfo

}