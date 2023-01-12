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

    override fun set(key: String, value: String?) {
        mLiveData[key] = MutableLiveData(value)
        commitListener?.onCommit()
    }

    override val keys: Set<String>
        get() = mLiveData.keys.toSet()

    override fun get(key: String): String? {
        return mLiveData[key]?.getValue().unsafeCast<String?>()
    }

    @Deprecated("This should NOT be used with MVVM. Data should be returned via bus")
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