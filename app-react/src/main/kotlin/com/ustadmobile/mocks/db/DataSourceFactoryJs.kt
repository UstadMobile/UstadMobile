package com.ustadmobile.mocks.db

import androidx.paging.DataSource

class DataSourceFactoryJs<Key,Value>(private val values:List<Value>): DataSource.Factory<Key,Value>() {

    override suspend fun getData(offset: Int, limit: Int): List<Value> {
        return values
    }
}