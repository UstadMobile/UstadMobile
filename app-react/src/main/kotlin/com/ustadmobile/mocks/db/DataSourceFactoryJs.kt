package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.util.Util.loadDataAsList
import kotlinx.serialization.DeserializationStrategy

class DataSourceFactoryJs<Key,Value, EXtra>(private val key:String? = null,
                                            private val filterBy: Any,
                                            private val sourcePath: String,
                                            private val dStrategy: DeserializationStrategy<List<Value>>,
                                            private val extraStrategy: DeserializationStrategy<List<EXtra>>? = null,
                                            private val targetKey: String? = null,
                                            private val relationKey: String? = null,
                                            private val extraKey:String? = null,
                                            private val extraSourcePath:String? = null
): DataSource.Factory<Key,Value>() {

    override suspend fun getData(offset: Int, limit: Int): List<Value> {
        var dataSet = loadDataAsList(sourcePath,dStrategy)

        if(sourcePath == "people"){
            return listOf(PersonWithDisplayDetails().apply {
                personUid = filterBy.toString().toLong()
                username = "admin"
                firstNames = "Admin"
                admin = true
                lastName = "Users"
            } as Value)
        }

        if(sourcePath == "roles"){
            return listOf(EntityRoleWithNameAndRole().apply {
            } as Value)
        }

        if(key != null && dataSet.isNotEmpty()){
            dataSet = dataSet.filter{it.asDynamic()[key].toString() == filterBy.toString()}
        }

        if(relationKey != null && extraSourcePath != null){
            val extraDatSet = loadDataAsList(extraSourcePath,extraStrategy!!)
            dataSet = dataSet.map { data ->
                val found = extraDatSet.firstOrNull{ foundDataSet ->
                    foundDataSet.asDynamic()[extraKey].toString() == data.asDynamic()[relationKey].toString()
                }
                if(found != null){
                    data.asDynamic()[targetKey] = found
                }
                data
            }.toMutableList()
        }
        return dataSet
    }
}