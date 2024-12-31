package com.ustadmobile.libcache.distributed

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Initialize DistributedCacheNsdAndroid with a LifecycleOwner so discovery will run only when the
 * lifecycle is at least started and the service will be registered when the lifecycle is created.
 *
 * @param distCacheNsd function that returns DistributedCacheNsdAndroid. This may invoke IO
 *        activity, so it will be run on the IO dispatcher.
 */
fun LifecycleOwner.launchInitDistributedCacheNsdWithLifecycle(
    distCacheNsd: () -> DistributedCacheNsdAndroid
) {
    lifecycleScope.launch(Dispatchers.IO) {
        val cacheNsdAndroid = distCacheNsd()
        withContext(Dispatchers.Main) {
            cacheNsdAndroid.initWithLifecycleOwner(this@launchInitDistributedCacheNsdWithLifecycle)
        }
    }
}
