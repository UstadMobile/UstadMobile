package com.ustadmobile.core.domain.localsharing.setenabled

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SetLocalSharingEnabledUseCase(
    private val settings: Settings
) {

    operator fun invoke(enabled: Boolean) {
        settings[LOCAL_SHARING_ENABLED] = enabled
    }

    companion object {

        const val LOCAL_SHARING_ENABLED = "local_sharing_enabled"

    }

}