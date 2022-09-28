package com.ustadmobile.core.navigation

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData

class UstadSavedStateHandleJs(
    initialValues: Map<String, String>? = null,
    private val commitListener: CommitListener? = null,
): UstadSavedStateHandle {

    interface CommitListener {
        fun onCommit()
    }

    internal val mLiveData: MutableMap<String, MutableLiveData<*>?> = mutableMapOf()

    internal val currentValues: Map<String, String>
        get() {
            return mLiveData.map {
                it.key to (it.value?.getValue()?.toString())
            }.filter { it.second != null }.associate { it.unsafeCast<Pair<String, String>>() }
        }

    init {
        initialValues?.forEach {
            mLiveData[it.key] = MutableLiveData(it.value)
        }
    }

    override fun <T> set(key: String, value: T?) {
        mLiveData[key] = MutableLiveData(value)
        commitListener?.onCommit()
    }


    override fun <T> get(key: String): T? {
        return mLiveData[key]?.getValue().unsafeCast<T>()
    }

    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        return mLiveData.getOrPut(key) {
            MutableLiveData(null)
        }.unsafeCast<MutableLiveData<T>>()
    }

    fun dumpToString(): String {
        return mLiveData.entries.map { it.key to it.value?.getValue() }
            .joinToString { "${it.first}=${it.second}" }
    }

}