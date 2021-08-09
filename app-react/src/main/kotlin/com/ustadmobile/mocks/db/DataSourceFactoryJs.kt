package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.mocks.DoorLiveDataJs

class DataSourceFactoryJs<Key,Value>(private val values:List<Value>): DoorDataSourceFactory<Key,Value>() {

    override fun getData(_offset: Int, _limit: Int): DoorLiveData<List<Value>> {
        return DoorLiveDataJs(values)
    }

    override fun getLength(): DoorLiveData<Int> {
        return DoorLiveDataJs(values.size)
    }
}