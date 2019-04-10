package com.ustadmobile.core.db

/**
 * Created by mike on 1/14/18.
 */

interface UmObserver<T> {

    fun onChanged(t: T)
}
