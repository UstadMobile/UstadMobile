package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.navigation.UstadSavedStateHandleJs
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json


fun UstadSavedStateHandle.setAllFromMap(stringMap: Map<String, String>) {
    stringMap.forEach {
        set(it.key, it.value)
    }
}


fun <T> UstadSavedStateHandle.observeResult(lifecycleOwner: LifecycleOwner,
                                            serializer: KSerializer<T>,
                                            resultKey: String,
                                            block: (List<T>) -> Unit) {
    getLiveData<String?>(resultKey).observe(lifecycleOwner) {
        if(!it.isNullOrBlank()){
            val entity = Json.decodeFromString(ListSerializer(serializer), it)
            set(resultKey, null)
            block(entity)
        }
    }
}

fun UstadSavedStateHandleJs.toStringMap() : Map<String, String> {
    return mutableMapOf<String, String>().also {
        this.mLiveData.keys.forEach {key ->
            val strVal = get<String>(key)
            if(strVal != null)
                it[key] = strVal
        }
    }
}