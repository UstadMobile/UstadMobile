package com.ustadmobile.core.domain.localsharing.devicename

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SetDeviceNameUseCase(
    private val settings: Settings,
) {

    operator fun invoke(deviceName: String) {
        settings[KEY_DEVICE_NAME] = deviceName
    }

    companion object {

        const val KEY_DEVICE_NAME = "localsharing_device_name"

    }

}