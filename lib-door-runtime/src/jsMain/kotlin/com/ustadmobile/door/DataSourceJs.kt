package com.ustadmobile.door

import androidx.paging.DataSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class DataSourceJs<K, V> (val fetchFn: suspend(_offset: Int, _limit: Int) -> List<V>): DataSource<K, V>() {

    @JsName("load")
    fun load(_offset: Int, _limit: Int, callback: (err: Exception?, items: List<V>?) -> Unit) {
        GlobalScope.async {
            try {
                callback(null, fetchFn(_offset, _limit))
            }catch(e: Exception) {
                callback(e, null)
            }
        }
    }

}