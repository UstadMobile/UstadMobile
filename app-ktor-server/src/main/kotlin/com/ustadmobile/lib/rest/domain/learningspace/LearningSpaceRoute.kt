package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.lib.rest.domain.learningspace.create.CreateLearningSpaceUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import org.kodein.di.DI
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di

fun Route.LearningSpaceRoute(
    createLearningSpaceUseCase: CreateLearningSpaceUseCase
) {
    post("create") {
        val request = call.receive<CreateLearningSpaceUseCase.CreateLearningSpaceRequest>()
        val di: DI by call.application.closestDI()

        createLearningSpaceUseCase(request)
        call.respondText(
            text = "OK",
            status = HttpStatusCode.OK
        )
    }

}

