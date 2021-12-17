package com.ustadmobile.core.impl

import com.ustadmobile.core.util.StorageUtil

actual class ContainerStorageManager {

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