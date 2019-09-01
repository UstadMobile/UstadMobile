package db2

import io.ktor.client.call.receive
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.takeFrom
import kotlin.Int
import kotlin.collections.List

class ExampleEntityPkIntDao {
    override fun insertAndReturnId(entity: ExampleEntityPkInt): Int {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleEntityPkIntDao/insertAndReturnId"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Int>()
        return _httpResult
    }

    override fun insertListAndReturnIds(entityList: List<ExampleEntityPkInt>): List<Int> {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath =
                        "${encodedPath}${_dbPath}/ExampleEntityPkIntDao/insertListAndReturnIds"
            }
            body = defaultSerializer().write(entityList)
        }
        val _httpResult = _httpResponse.receive<List<Int>>()
        return _httpResult
    }

    override fun findByPk(pk: Int): ExampleEntityPkInt? {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleEntityPkIntDao/findByPk"
            }
            parameter("pk", pk)
        }
        val _httpResult = _httpResponse.receive<ExampleEntityPkInt>()
        return _httpResult
    }
}
