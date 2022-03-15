package com.ustadmobile.core.impl

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.StorageUtil
import org.kodein.di.DI

actual class ContainerStorageManager(
    endpoint: Endpoint,
    di: DI
) {

    private val storages = mutableListOf<ContainerStorageDir>()

    init {
        val storage = StorageUtil.getStorage()
        repeat(storage.count()) {
            storages.add(ContainerStorageDir("/"))
        }
    }

    actual val storageList: List<ContainerStorageDir>
        get() = storages


}