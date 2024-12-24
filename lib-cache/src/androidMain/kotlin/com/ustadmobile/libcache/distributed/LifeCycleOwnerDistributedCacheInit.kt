package com.ustadmobile.libcache.distributed

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Initialize DistributedCacheNsdAndroid with a LifecycleOwner
 *
 * @param distCacheProvider function that returns DistributedCacheNsdAndroid. This may invoke IO
 *        activity, so it will be run on the IO dispatcher.
 *
 */
fun LifecycleOwner.launchDistributedCacheNsdInit(
    distCacheProvider: () -> DistributedCacheNsdAndroid
) {
    lifecycleScope.launch(Dispatchers.IO) {
        val cacheNsdAndroid = distCacheProvider()
        withContext(Dispatchers.Main) {
            cacheNsdAndroid.initWithLifecycleOwner(this@launchDistributedCacheNsdInit)
        }
    }
}
