package com.ustadmobile.systemdb.datasource.network

import com.ustadmobile.systemdb.datasource.LearningSpaceDataSource
import com.ustadmobile.systemdb.datasource.SystemDbDataSource
import io.ktor.client.HttpClient

class SystemDbDataSourceHttp(
    private val url: String,
    private val httpClient: HttpClient,
) : SystemDbDataSource{

    override val learningSpaceDataSource: LearningSpaceDataSource by lazy {
        LearningSpaceDataSourceHttp("${url}${LearningSpaceDataSource.PATH}/", httpClient)
    }
}