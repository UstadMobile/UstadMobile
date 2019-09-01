package db2

import androidx.paging.DataSource
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorQuery
import io.ktor.client.call.receive
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.takeFrom
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

class ExampleDao2 {
    override fun insertAndReturnId(entity: ExampleEntity2): Long {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/insertAndReturnId"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Long>()
        return _httpResult
    }

    override suspend fun insertAsync(entity: ExampleEntity2) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/insertAsync"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override suspend fun insertAsyncAndGiveId(entity: ExampleEntity2): Long {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/insertAsyncAndGiveId"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Long>()
        return _httpResult
    }

    override fun insertList(entityList: List<out ExampleEntity2>) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/insertList"
            }
            body = defaultSerializer().write(entityList)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun insertOtherList(entityList: List<out ExampleEntity2>) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/insertOtherList"
            }
            body = defaultSerializer().write(entityList)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun insertAndReturnList(entityList: List<out ExampleEntity2>): List<Long> {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/insertAndReturnList"
            }
            body = defaultSerializer().write(entityList)
        }
        val _httpResult = _httpResponse.receive<List<Long>>()
        return _httpResult
    }

    override fun insertListAndReturnIdsArray(entityList: List<out ExampleEntity2>): Array<Long> {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/insertListAndReturnIdsArray"
            }
            body = defaultSerializer().write(entityList)
        }
        val _httpResult = _httpResponse.receive<Array<Long>>()
        return _httpResult
    }

    override fun replace(entityList: List<out ExampleEntity2>) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/replace"
            }
            body = defaultSerializer().write(entityList)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun findByUid(uid: Long): ExampleEntity2? {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findByUid"
            }
            parameter("uid", uid)
        }
        val _httpResult = _httpResponse.receive<ExampleEntity2>()
        return _httpResult
    }

    override suspend fun findLarge(uid: Long, min: Long): ExampleEntity2? {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findLarge"
            }
            parameter("uid", uid)
            parameter("min", min)
        }
        val _httpResult = _httpResponse.receive<ExampleEntity2>()
        return _httpResult
    }

    override suspend fun findLargeAsync(uid: Long, min: Long): List<ExampleEntity2> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findLargeAsync"
            }
            parameter("uid", uid)
            parameter("min", min)
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override fun findWithNullableParam(name: String?): List<ExampleEntity2> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findWithNullableParam"
            }
            if(name != null) {
                parameter("name", name)
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override fun findNameByUid(uid: Long): String? {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findNameByUid"
            }
            parameter("uid", uid)
        }
        val _httpResult = _httpResponse.receive<String>()
        return _httpResult
    }

    override fun findByUidWithLinkEntity(uid: Long): ExampleEntity2WithExampleLinkEntity? {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findByUidWithLinkEntity"
            }
            parameter("uid", uid)
        }
        val _httpResult = _httpResponse.receive<ExampleEntity2WithExampleLinkEntity>()
        return _httpResult
    }

    override fun findAll(): List<ExampleEntity2> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findAll"
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override suspend fun findAllAsync(): List<ExampleEntity2WithExampleLinkEntity> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findAllAsync"
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2WithExampleLinkEntity>>()
        return _httpResult
    }

    override fun updateSingleItem(entity: ExampleEntity2) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/updateSingleItem"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun updateSingleItemAndReturnCount(entity: ExampleEntity2): Int {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/updateSingleItemAndReturnCount"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Int>()
        return _httpResult
    }

    override fun updateList(updateEntityList: List<out ExampleEntity2>) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/updateList"
            }
            body = defaultSerializer().write(updateEntityList)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun findByMinUidLive(): DoorLiveData<List<ExampleEntity2>> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/findByMinUidLive"
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override fun updateByParam(newName: String, num: Long): Int {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/updateByParam"
            }
            parameter("newName", newName)
            parameter("num", num)
        }
        val _httpResult = _httpResponse.receive<Int>()
        return _httpResult
    }

    override fun updateByParamNoReturn(newName: String, number: Long) {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/updateByParamNoReturn"
            }
            parameter("newName", newName)
            parameter("number", number)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun deleteSingle(entity: ExampleEntity2) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/deleteSingle"
            }
            body = defaultSerializer().write(entity)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun deleteList(deleteList: List<out ExampleEntity2>) {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/deleteList"
            }
            body = defaultSerializer().write(deleteList)
        }
        val _httpResult = _httpResponse.receive<Unit>()
    }

    override fun countNumEntities(): Int {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/countNumEntities"
            }
        }
        val _httpResult = _httpResponse.receive<Int>()
        return _httpResult
    }

    override fun queryUsingArray(uidList: List<Long>): List<ExampleEntity2> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/queryUsingArray"
            }
            parameter("uidList", uidList)
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override fun rawQueryForList(query: DoorQuery): List<ExampleEntity2> {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/rawQueryForList"
            }
            body = defaultSerializer().write(query)
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override fun rawQueryForSingleValue(query: DoorQuery): ExampleEntity2? {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/rawQueryForSingleValue"
            }
            body = defaultSerializer().write(query)
        }
        val _httpResult = _httpResponse.receive<ExampleEntity2>()
        return _httpResult
    }

    override fun queryAllLive(): DataSource.Factory<Int, ExampleEntity2> {
        val _httpResponse = _httpClient.get<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/queryAllLive"
            }
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }

    override fun rawQueryWithArrParam(query: DoorQuery): List<ExampleEntity2> {
        val _httpResponse = _httpClient.post<HttpResponse> {
            url {
                takeFrom(_endpoint)
                encodedPath = "${encodedPath}${_dbPath}/ExampleDao2/rawQueryWithArrParam"
            }
            body = defaultSerializer().write(query)
        }
        val _httpResult = _httpResponse.receive<List<ExampleEntity2>>()
        return _httpResult
    }
}
