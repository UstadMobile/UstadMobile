package com.ustadmobile.core.util.ext

import com.ustadmobile.door.paging.LoadParams
import com.ustadmobile.door.paging.LoadResult
import app.cash.paging.PagingSource

suspend fun <Key: Any, Value: Any> PagingSource<Key, Value>.loadFirstList(): List<Value> {
    val loadParams: LoadParams<Key> = LoadParams.Refresh(
        key = null, loadSize = 50, placeholdersEnabled = true
    )
    val loadResult = load(loadParams)
    return (loadResult as LoadResult.Page).data
}
