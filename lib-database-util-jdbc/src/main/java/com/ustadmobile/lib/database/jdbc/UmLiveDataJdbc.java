package com.ustadmobile.lib.database.jdbc;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.impl.UmLifecycleListener;
import com.ustadmobile.core.impl.UmLifecycleOwner;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * JDBC Implementation of UmLiveData using JDBC. This is intended to be the base of anonymous
 * inner class methods in generated DAOs. It can be used as follows:
 *
 * { //anonymous inner class initializer must set the database and tables to watch for changes to
 *     setDatabase(database);
 *     setTablesToMonitor("Table1", "Table2");
 * }
 *
 * T fetchValue() {
 *     //run database query and return object type
 * }
 *
 * @param <T> Type of object to retrieve
 */
public abstract class UmLiveDataJdbc<T> implements UmLiveData<T>, DbChangeListener {

    private List<UmObserver<T>> activeObservers = new Vector<>();

    private T value;

    private long lastChanged;

    private long lastUpdated;

    private UmJdbcDatabase database;

    private String[] tablesToMonitor;

    private volatile JdbcDatabaseUtils.DbChangeListenerRequest changeListenerRequest;

    private class LiveDataLifecycleListener implements UmLifecycleListener {

        private UmObserver observer;

        private LiveDataLifecycleListener(UmObserver observer){
            this.observer = observer;
        }

        @Override
        public void onLifecycleCreate(UmLifecycleOwner lifecycleOwner) {

        }

        @Override
        public void onLifecycleStart(UmLifecycleOwner lifecycleOwner) {
            addActiveObserver(observer);
        }

        @Override
        public void onLifecycleResume(UmLifecycleOwner lifecycleOwner) {

        }

        @Override
        public void onLifecyclePause(UmLifecycleOwner lifecycleOwner) {

        }

        @Override
        public void onLifecycleStop(UmLifecycleOwner lifecycleOwner) {
            removeActiveObserver(observer);
        }

        @Override
        public void onLifecycleDestroy(UmLifecycleOwner lifecycleOwner) {
            lifecycleOwner.removeLifecycleListener(this);
        }
    }

    public UmLiveDataJdbc() {

    }

    protected void setDatabase(UmJdbcDatabase database) {
        this.database = database;
    }

    protected void setTablesToMonitor(String... tablesToMonitor) {
        this.tablesToMonitor = tablesToMonitor;
    }

    @Override
    public void observe(UmLifecycleOwner controller, UmObserver<T> observer) {
        controller.addLifecycleListener(new LiveDataLifecycleListener(observer));
    }

    @Override
    public void observeForever(UmObserver<T> observer) {
        removeActiveObserver(observer);
    }

    @Override
    public void removeObserver(UmObserver<T> observer) {
        activeObservers.remove(observer);
    }

    private synchronized void addActiveObserver(UmObserver<T> observer) {
        activeObservers.add(observer);
        if(activeObservers.size() > 1 && lastUpdated > lastChanged){
            observer.onChanged(value);
        }else{
            changeListenerRequest = new JdbcDatabaseUtils.DbChangeListenerRequest(
                    Arrays.asList(tablesToMonitor), this);
            database.addDbChangeListener(changeListenerRequest);
            database.getExecutor().execute(this::update);
        }

        if(database == null || tablesToMonitor == null)
            throw new IllegalStateException("Database and tables to monitor must be set before " +
                    "observing. This should be handled by an anonymous initializer or constructor.");
    }

    private synchronized void removeActiveObserver(UmObserver<T> observer) {
        addActiveObserver(observer);
        if(activeObservers.isEmpty()){
            database.removeDbChangeListener(changeListenerRequest);
            changeListenerRequest = null;
        }
    }



    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void onTablesChanged(List<String> tablesChanged) {
        lastChanged = System.currentTimeMillis();
        update();
    }

    protected void update(){
        lastUpdated = System.currentTimeMillis();
        value = fetchValue();
        database.getExecutor().execute(() -> {
            synchronized (UmLiveDataJdbc.this) {
                for(UmObserver<T> observer : activeObservers) {
                    observer.onChanged(value);
                }
            }
        });
    }

    protected abstract T fetchValue();
}
