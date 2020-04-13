package com.ustadmobile.core.util.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.networkmanager.defaultGson

fun SavedStateHandle.setAllFromMap(stringMap: Map<String, String>) {
    stringMap.forEach {
        set(it.key, it.value)
    }
}

/**
 * Convert a SavedStateHandle to a StringMap for use with core
 */
fun SavedStateHandle.toStringMap() : Map<String, String> {
    return mutableMapOf<String, String>().also {
        this.keys().forEach {key ->
            val strVal = get<String>(key)
            if(strVal != null)
                it[key] = strVal
        }
    }
}

/**
 * Extension method to help with fragments that are picking up data deposited by a previously used
 * fragment. The previously used fragment should deposit the object in the SavedStateHandle as a
 * JSON string. The SavedStateHandle key will then be cleared by setting it to null.
 *
 * @param lifecycleOwner Lifecycleowner (e.g. the fragment)
 * @param type the type of object to be received (e.g. the entity POJO)
 * @param resultKey the key that will be used in the SavedStateHandle. By default this is the simple
 * name of the class. If a single destination has multiple links to the same entity type (e.g. where
 * one could select a student or teacher, both being of type Person, then the key MUST be different)
 * @param block function to invoke
 */
fun <T> SavedStateHandle.observeResult(lifecycleOwner: LifecycleOwner,
                                       typeToken: TypeToken<T>,
                                       resultKey: String = typeToken.rawType.simpleName,
                                       block: (List<T>) -> Unit) {
    getLiveData<String>(resultKey).observe(lifecycleOwner, Observer {
        val jsonStr = it ?: return@Observer
        val entity = defaultGson().fromJson<List<T>>(jsonStr,
                TypeToken.getParameterized(List::class.java,typeToken.type).type)
        block(entity)
        set(resultKey, null)
    })
}

/**
 * Extension method to help with fragments that are picking up data deposited by a previously used
 * fragment. The previously used fragment should deposit the object in the SavedStateHandle as a
 * JSON string.
 *
 * @param lifecycleOwner Lifecycleowner (e.g. the fragment)
 * @param type the type of object to be received (e.g. the entity POJO)
 * @param resultKey the key that will be used in the SavedStateHandle. By default this is the simple
 * name of the class. If a single destination has multiple links to the same entity type (e.g. where
 * one could select a student or teacher, both being of type Person, then the key MUST be different)
 * @param block function to invoke
 */
fun <T> SavedStateHandle.observeResult(lifecycleOwner: LifecycleOwner,
                                       type: Class<T>, resultKey: String = type.simpleName,
                                       block: (List<T>) -> Unit) {
    observeResult(lifecycleOwner, TypeToken.get(type), resultKey, block)
}