package com.ustadmobile.util.ext

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.navigation.UstadSavedStateHandleJs
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json


fun UstadSavedStateHandle.setAllFromMap(stringMap: Map<String, String>) {
    stringMap.forEach {
        set(it.key, it.value)
    }
}


fun <T> UstadSavedStateHandle.observeResult(lifecycleOwner: DoorLifecycleOwner,
                                            serializer: KSerializer<T>,
                                            resultKey: String = serializer.descriptor.serialName,
                                            block: (List<T>) -> Unit) {
    getLiveData<String>(resultKey).observe(lifecycleOwner) {
        val entity = Json.decodeFromString(ListSerializer(serializer), it)
        block(entity)
        set(resultKey, null)
    }
}

fun UstadSavedStateHandleJs.toStringMap() : Map<String, String> {
    return mutableMapOf<String, String>().also {
        this.mLiveDatas.keys.forEach {key ->
            val strVal = get<String>(key)
            if(strVal != null)
                it[key] = strVal
        }
    }
}