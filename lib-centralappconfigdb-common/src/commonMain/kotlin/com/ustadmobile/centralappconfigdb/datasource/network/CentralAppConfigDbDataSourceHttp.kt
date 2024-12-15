package com.ustadmobile.centralappconfigdb.datasource.network

import com.ustadmobile.centralappconfigdb.datasource.LearningSpaceDataSource
import com.ustadmobile.centralappconfigdb.datasource.CentralAppConfigDbDataSource
import io.ktor.client.HttpClient

class CentralAppConfigDbDataSourceHttp(
    private val url: String,
    private val httpClient: HttpClient,
) : CentralAppConfigDbDataSource{

    override val learningSpaceDataSource: LearningSpaceDataSource by lazy {
        LearningSpaceDataSourceHttp("${url}${LearningSpaceDataSource.PATH}/", httpClient)
    }
}