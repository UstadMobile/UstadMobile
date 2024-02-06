package com.ustadmobile.core.launchopenlicenses

import com.ustadmobile.core.domain.htmlcontentdisplayengine.LaunchChromeUseCase
import com.ustadmobile.core.domain.launchopenlicenses.LaunchOpenLicensesUseCase
import java.awt.Desktop
import java.io.File

class LaunchOpenLicensesUseCaseJvm(
    private val launchChromeUseCase: LaunchChromeUseCase,
    private val licenseFile: File,
): LaunchOpenLicensesUseCase {

    override suspend fun invoke() {
        try {
            launchChromeUseCase(licenseFile.toURI().toURL().toString())
        }catch(e: Throwable) {
            Desktop.getDesktop().browse(licenseFile.toURI())
        }
    }

}