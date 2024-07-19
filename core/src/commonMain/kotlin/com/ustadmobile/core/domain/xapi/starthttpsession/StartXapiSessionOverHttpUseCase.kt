package com.ustadmobile.core.domain.xapi.starthttpsession

import com.ustadmobile.core.domain.xapi.XapiSession
import kotlinx.serialization.Serializable

/**
 * Used to run an XapiSession over Http. Sometimes an XapiSession will be purely internal (e.g.
 * where the user is watching a video, reading a PDF, etc). In this case Xapi Statements are just
 * stored directly in the database.
 *
 * Where external content / tools are used they must access the Xapi endpoint using Http. This will
 * require an http url (on Android/Desktop this is the embedded server, on JS this is the online
 * main http server).
 */
interface StartXapiSessionOverHttpUseCase {

    @Serializable
    data class StartXapiSessionOverHttpResult(
        val basicAuth: String,
        val httpUrl: String,
    )

    suspend operator fun invoke(
        xapiSession: XapiSession,
    ): StartXapiSessionOverHttpResult

}