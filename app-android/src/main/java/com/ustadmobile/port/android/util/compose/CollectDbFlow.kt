package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Collect a flow from the active database. Nullable so this can be used without an active database
 * (e.g. preview mode).
 *
 * @param key1 remember parameter (e.g. query key)
 * @param initialValue initial value to use for flow collection as state
 * @parma db UmAppDatabase (optional - if already looked up)
 * @param flowAdapter function that will return a flow from the given database
 *
 * @return collection of the flow
 */
@Composable
fun <T> collectDbFlow(
    key1: Any?,
    initialValue: T,
    db: UmAppDatabase? = rememberActiveDatabase(tag = DoorTag.TAG_DB),
    flowAdapter: (UmAppDatabase) -> Flow<T>
) : T {
    val flow = remember(key1, db) {
        db?.let { flowAdapter(it) } ?: emptyFlow()
    }

    val flowState: T by flow.collectAsState(initial = initialValue)

    return flowState
}
