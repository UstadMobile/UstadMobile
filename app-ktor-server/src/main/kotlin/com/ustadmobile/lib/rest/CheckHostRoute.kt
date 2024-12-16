package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ext.respondRequestUrlNotMatchingSiteConfUrl
import com.ustadmobile.lib.rest.ext.urlMatchesLearningSpace
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.routing.Route


/**
 * Add an intercept to check that the url being used (e.g. in the browser) matches the system
 * configuration - see urlMatchesConfig and ApplicationCall.callEndpoint
 */
fun Route.addHostCheckIntercept() {
    intercept(ApplicationCallPipeline.Plugins) {
        if(!context.urlMatchesLearningSpace()) {
            context.respondRequestUrlNotMatchingSiteConfUrl()
            return@intercept finish()
        }
    }
}
