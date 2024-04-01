package com.ustadmobile.lib.rest

import io.ktor.server.routing.Route
import io.ktor.server.routing.route

/**
 * If the prefix is not null, then the route will be prefixed/enclosed accoringly.
 *
 * @param prefix if non-null, then the build route will be applied within a route. If null, then the
 *        route will be applied directly.
 * @param build route to build
 */
fun Route.prefixRoute(
    prefix: String?,
    build: Route.() -> Unit,
) {
    if(prefix != null) {
        //The route must not end with "/", otherwise we would be looking for //path instead /path
        route(prefix.removeSuffix("/"), build)
    }else {
        apply(build)
    }
}