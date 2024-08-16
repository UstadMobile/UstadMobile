package com.ustadmobile.core.domain.getapiurl

/**
 * Get a URL for an API (e.g. xAPI, OneRoster, etc). On Android/Desktop this will be on the embedded
 * server. On app-ktor-server (e.g. main http server), this will be the server's real url.
 *
 * On the main http server these urls will be accessed as /api/apiname/path
 */
interface GetApiUrlUseCase {

    /**
     *
     * @param path the api url e.g. "/api/xapi/statements?id=..."
     * @return The URL to be used e.g. https://example.org/api/xapi/statements?id=... if direct to
     *         main server, or http://localhost:port/encoded-endpoint/api/xapi/statements?id=... if
     *         using an embedded server (e.g. Desktop and Android).
     */
    operator fun invoke(path: String): String

}