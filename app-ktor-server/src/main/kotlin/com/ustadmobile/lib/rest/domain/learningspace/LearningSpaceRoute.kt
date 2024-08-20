package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.lib.rest.domain.learningspace.create.CreateLearningSpaceUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.LearningSpaceRoute(
    createLearningSpaceUseCase: CreateLearningSpaceUseCase
) {
    post("create") {
        try {
            val request = call.receive<CreateLearningSpaceUseCase.CreateLearningSpaceRequest>()

            createLearningSpaceUseCase(request)
            call.respondText(
                text = "OK",
                status = HttpStatusCode.OK
            )
        }catch(e: Exception) {
            e.printStackTrace()
            e.printStackTrace()
        }

    }

}

