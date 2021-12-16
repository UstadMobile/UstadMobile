package com.ustadmobile.core.impl

import android.content.Context
import androidx.core.net.toUri
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.io.ext.siteDataSubDir
import org.kodein.di.DI
import org.kodein.di.instance

actual class ContainerStorageManager(
    context: Context,
    endpoint: Endpoint,
    di: DI
) {

    actual val storageList: List<ContainerStorageDir>

    private val systemImpl: UstadMobileSystemImpl by di.instance()

    init {
        val list = mutableListOf<ContainerStorageDir>()

        //Main phone memory
        val internalStorageFolder = context.filesDir.siteDataSubDir(endpoint)
        internalStorageFolder.takeIf { !it.exists() }?.mkdirs()
        list += ContainerStorageDir(internalStorageFolder.toUri().toString(),
            systemImpl.getString(MessageID.phone_memory, context), context.filesDir.usableSpace,
            false)

        context.getExternalFilesDir(null)?.also {
            it.mkdirs()
            list += ContainerStorageDir(it.toUri().toString(),
                    systemImpl.getString(MessageID.memory_card, context), it.usableSpace,
                    true)
        }

        storageList = list.toList()
    }

}