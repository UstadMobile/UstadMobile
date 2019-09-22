package com.ustadmobile.door

import androidx.paging.DataSource

class DataSourceFactoryJs<K,V>(val fetchFn: suspend(_offset: Int, _limit: Int) -> List<V>): DataSource.Factory<K,V>() {

    fun create() = DataSourceJs<K, V>(fetchFn)

}