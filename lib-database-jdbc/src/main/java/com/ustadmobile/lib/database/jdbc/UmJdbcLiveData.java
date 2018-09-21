package com.ustadmobile.lib.database.jdbc;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.impl.UmLifecycleOwner;

import java.util.List;
import java.util.Vector;

public abstract class UmJdbcLiveData<T> implements UmLiveData<T>, DbChangeListener {

    private List<UmObserver<T>> activeObservers = new Vector<>();

    private T value;

    private volatile boolean valid;

    private UmJdbcDatabase database;

    public UmJdbcLiveData(UmJdbcDatabase database) {
        this.database = database;
    }

    //TODO: implement this using UstadBaseController etc.
    @Override
    public void observe(UmLifecycleOwner controller, UmObserver<T> observer) {

    }

    @Override
    public void observeForever(UmObserver<T> observer) {
        activeObservers.add(observer);
        if(valid){
            observer.onChanged(value);
        }else{
            update();
        }
    }

    @Override
    public void removeObserver(UmObserver<T> observer) {
        activeObservers.remove(observer);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void onTablesChanged(List<String> tablesChanged) {
        valid = false;
        update();
    }

    protected void update(){
        /*TODO: this may need reconsidered to handle concurrency issues. Running onChanged should run in another thread */
        value = fetchValue();
        valid = true;
        database.getExecutor().execute(() -> {
            for(UmObserver<T> observer : activeObservers) {
                observer.onChanged(value);
            }
        });
    }

    protected abstract T fetchValue();
}
