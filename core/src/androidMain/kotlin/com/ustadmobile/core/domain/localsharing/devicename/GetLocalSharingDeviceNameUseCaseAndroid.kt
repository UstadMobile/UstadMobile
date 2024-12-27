package com.ustadmobile.core.domain.localsharing.devicename

import android.os.Build
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get

class GetLocalSharingDeviceNameUseCaseAndroid(
    private val settings: Settings,
): GetLocalSharingDeviceNameUseCase {

    override fun invoke(): String {
        return settings[SetDeviceNameUseCase.KEY_DEVICE_NAME] ?: "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}