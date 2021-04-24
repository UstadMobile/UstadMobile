package com.ustadmobile.db

import androidx.paging.DataSource
import com.ustadmobile.util.UmReactUtil.loadList
import kotlinx.serialization.DeserializationStrategy

class DataSourceFactoryJs<Key,Value, EXtra>(private val key:String? = null, private val filterBy: Any,
                                            private val sourcePath: String,
                                            private val dStrategy: DeserializationStrategy<List<Value>>,
                                            private val extraStrategy: DeserializationStrategy<List<EXtra>>? = null,
                                            private val targetKey: String? = null,
                                            private val relationKey: String? = null,
                                            private val extraKey:String? = null,
                                            private val extraSourcePath:String? = null
): DataSource.Factory<Key,Value>() {

    override suspend fun getData(offset: Int, limit: Int): List<Value> {
        var dataSet = loadList(sourcePath,dStrategy)

        if(key != null && dataSet.isNotEmpty()){
            dataSet = dataSet.filter{it.asDynamic()[key].toString() == filterBy.toString()}
        }

        if(relationKey != null && extraSourcePath != null){
            val extraDatSet = loadList(extraSourcePath,extraStrategy!!)
            dataSet = dataSet.map {
                val found = extraDatSet.firstOrNull{
                        it2 -> it2.asDynamic()[extraKey].toString() == it.asDynamic()[relationKey].toString()}
                if(found != null){
                    it.asDynamic()[targetKey] = found
                }
                it
            }.toMutableList()
        }
        return dataSet
    }
}