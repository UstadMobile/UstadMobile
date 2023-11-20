package com.ustadmobile.core.util.ext

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadParamsRefresh
import app.cash.paging.PagingSourceLoadResultPage

suspend fun <Key: Any, Value: Any> PagingSource<Key, Value>.loadFirstList(): List<Value> {
    val loadParams: PagingSourceLoadParams<Key> = PagingSourceLoadParamsRefresh(
        key = null, loadSize = 50, placeholdersEnabled = true
    )
    val loadResult = load(loadParams)
    return (loadResult as PagingSourceLoadResultPage).data
}
