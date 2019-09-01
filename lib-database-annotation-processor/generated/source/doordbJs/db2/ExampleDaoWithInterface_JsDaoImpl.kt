package db2

import io.ktor.client.call.receive
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.takeFrom
import kotlin.Int
import kotlin.Long
import kotlin.collections.List

class ExampleDaoWithInterface {
    override fun anotherQuery(param: Int): List<ExampleEntity2> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDaoWithInterface/anotherQuery"
            }
            parameter("param", param)
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override fun insertOne(entity: ExampleEntity2): Long {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDaoWithInterface/insertOne"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Long>()
        return _httpResult
    }
}
