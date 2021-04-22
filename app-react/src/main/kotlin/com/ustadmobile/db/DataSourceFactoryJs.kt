package com.ustadmobile.db

import androidx.paging.DataSource
import com.ustadmobile.util.UmReactUtil.loadList
import kotlinx.serialization.DeserializationStrategy

class DataSourceFactoryJs<Key,Value>(private val key:String? = null, private val filterBy: Any,
                                     private val sourcePath: String,
                                     private val dStrategy: DeserializationStrategy<List<Value>>): DataSource.Factory<Key,Value>() {

    override suspend fun getData(offset: Int, limit: Int): List<Value> {
        var dataSet = loadList(sourcePath,dStrategy)
        if(key != null && dataSet.isNotEmpty()){
            dataSet = dataSet.filter{it.asDynamic()[key].toString() == filterBy.toString()}
        }
        return dataSet
    }
}