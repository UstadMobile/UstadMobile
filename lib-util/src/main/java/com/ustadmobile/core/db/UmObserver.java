package com.ustadmobile.core.db;

/**
 * Created by mike on 1/14/18.
 */

public interface UmObserver<T> {

    void onChanged(T t);
}
