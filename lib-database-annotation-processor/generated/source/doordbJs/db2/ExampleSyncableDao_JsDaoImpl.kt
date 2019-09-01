package db2

import androidx.paging.DataSource
import com.ustadmobile.door.DoorLiveData
import io.ktor.client.call.receive
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.takeFrom
import kotlin.Int
import kotlin.Long
import kotlin.Unit
import kotlin.collections.List

class ExampleSyncableDao {
    override fun insert(syncableEntity: ExampleSyncableEntity): Long {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleSyncableDao/insert"
            }
            body = defaultSerializer().write(syncableEntity)
        }
        val _httpResult = _httpResponse.receive<Long>()
        return _httpResult
    }

    override fun findAll(): List<ExampleSyncableEntity> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleSyncableDao/findAll"
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleSyncableEntity>>()
        return _httpResult
    }

    override fun findByUid(uid: Long): ExampleSyncableEntity? {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleSyncableDao/findByUid"
            }
            parameter("uid", uid)
        }
        val _httpResult = _httpResponse.receive<ExampleSyncableEntity>()
        return _httpResult
    }

    override fun findAllWithOtherByUid(): List<ExampleSyncableEntityWithOtherSyncableEntity> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleSyncableDao/findAllWithOtherByUid"
            }
        }
        val _httpResult =
                _httpResponse.receive<List<ExampleSyncableEntityWithOtherSyncableEntity>>()
        return _httpResult
    }

    override fun findAllLive(): DoorLiveData<List<ExampleSyncableEntity>> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleSyncableDao/findAllLive"
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleSyncableEntity>>()
        return _httpResult
    }

    override fun findAllDataSource(): DataSource.Factory<Int, ExampleSyncableEntity> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleSyncableDao/findAllDataSource"
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleSyncableEntity>>()
        return _httpResult
    }

    override fun updateNumberByUid(uid: Long, newNumber: Long) {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleSyncableDao/updateNumberByUid"
            }
            parameter("uid", uid)
            parameter("newNumber", newNumber)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }
}
