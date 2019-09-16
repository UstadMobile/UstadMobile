package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.model.HeadResponse

expect class PlatformHttpClient {

    fun headRequest(urlString: String): HeadResponse

}