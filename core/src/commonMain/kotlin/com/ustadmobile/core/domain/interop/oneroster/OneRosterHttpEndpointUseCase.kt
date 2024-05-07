package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.http.DoorJsonRequest
import com.ustadmobile.door.http.DoorJsonResponse

class OneRosterHttpEndpointUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase,
) {

    suspend operator fun invoke(request: DoorJsonRequest): DoorJsonResponse {
        return DoorJsonResponse(
            bodyText = "Not found",
            responseCode = 404,
            contentType = "text/plain",
        )
    }

}