package com.ustadmobile.lib.rest.domain.learningspace

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.LearningSpaceClientRoute(
    learningSpaceServerRepo: LearningSpaceServerRepo,
) {
    get("getAll") {
        call.respond(learningSpaceServerRepo.getAll())
    }


}