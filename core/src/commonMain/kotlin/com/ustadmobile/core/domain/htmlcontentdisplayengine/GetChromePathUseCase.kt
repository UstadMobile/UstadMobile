package com.ustadmobile.core.domain.htmlcontentdisplayengine

interface GetChromePathUseCase {

    suspend operator fun invoke(): String?

}