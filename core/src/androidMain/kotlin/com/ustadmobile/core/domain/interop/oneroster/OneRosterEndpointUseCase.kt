package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.isimplerequest.ISimpleTextRequest
import com.ustadmobile.door.http.DoorJsonResponse

/**
 * Scoped OneRosterEndpoint.
 */
class OneRosterEndpointUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase
) {

    suspend operator fun invoke(
        request: ISimpleTextRequest
    ): DoorJsonResponse {
        return DoorJsonResponse(
            bodyText = "HelloWorld OneRoster",
            contentType = "text/plain"
        )
    }


}