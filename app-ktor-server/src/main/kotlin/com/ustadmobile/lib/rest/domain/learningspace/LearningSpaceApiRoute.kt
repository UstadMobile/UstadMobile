package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.lib.rest.domain.learningspace.create.CreateLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.learningspace.delete.DeleteLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.learningspace.update.UpdateLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.systemconfig.verifyauth.VerifySystemConfigAuthUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.LearningSpaceApiRoute(
    verifySystemConfigAuthUseCase: VerifySystemConfigAuthUseCase,
    createLearningSpaceUseCase: CreateLearningSpaceUseCase,
    updateLearningSpaceUseCase: UpdateLearningSpaceUseCase,
    deleteLearningSpaceUseCase: DeleteLearningSpaceUseCase,
) {
    intercept(ApplicationCallPipeline.Setup) {
        try {
            verifySystemConfigAuthUseCase(call)
        }catch(e: HttpApiException) {
            call.respondText(
                text = e.message ?: "Unknown Error",
                status = HttpStatusCode.fromValue(e.statusCode)
            )

            return@intercept finish()
        }
    }

    post("create") {
        try {
            val request = call.receive<CreateLearningSpaceUseCase.CreateLearningSpaceRequest>()

            createLearningSpaceUseCase(request)
            call.respondText(
                text = "OK",
                status = HttpStatusCode.OK
            )
        }catch(e: Exception) {
            //TODO Here: return error response.
            e.printStackTrace()
        }

    }

    post("update") {
        try {
            val request = call.receive<UpdateLearningSpaceUseCase.UpdateLearningSpaceUseCase>()

            updateLearningSpaceUseCase(request)
            call.respondText(
                text = "OK",
                status = HttpStatusCode.OK
            )
        }catch(e: Exception) {
            //TODO Here: return error response.
            e.printStackTrace()
        }

    }
    post("delete") {
        try {
            val request = call.receive<DeleteLearningSpaceUseCase.DeleteLearningSpaceUseCase>()

            deleteLearningSpaceUseCase(request)
            call.respondText(
                text = "OK",
                status = HttpStatusCode.OK
            )
        }catch(e: Exception) {
            //TODO Here: return error response.
            e.printStackTrace()
        }

    }

}

