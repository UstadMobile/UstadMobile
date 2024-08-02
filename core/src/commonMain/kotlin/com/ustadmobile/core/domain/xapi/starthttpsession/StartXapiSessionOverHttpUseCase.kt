package com.ustadmobile.core.domain.xapi.starthttpsession

import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
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

    /**
     * @param auth complete authorization header e.g. "Basic <username:password base64 encoded>"
     * @param httpUrl the httpUrl that will be used to access the Xapi endpoint
     */
    @Serializable
    data class StartXapiSessionOverHttpResult(
        val auth: String,
        val httpUrl: String,
    )

    suspend operator fun invoke(
        xapiSession: XapiSessionEntity,
    ): StartXapiSessionOverHttpResult

}