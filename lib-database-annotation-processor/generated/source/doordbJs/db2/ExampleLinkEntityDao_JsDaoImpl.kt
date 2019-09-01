package db2

import io.ktor.client.call.receive
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.takeFrom
import kotlin.Unit

class ExampleLinkEntityDao {
    override fun insert(linkEntity: ExampleLinkEntity) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleLinkEntityDao/insert"
            }
            body = defaultSerializer().write(linkEntity)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }
}
