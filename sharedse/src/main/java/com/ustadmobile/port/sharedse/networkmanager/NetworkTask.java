package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by kileha3 on 08/05/2017.
 */

public abstract class NetworkTask {

    public NetworkTask(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public NetworkManagerTaskListener managerTaskListener;
    protected NetworkManager networkManager;
    public int queueId;
    public int taskId;
    public int taskType;
    public boolean useBluetooth;
    public boolean useHttp;

    public abstract void start();
    public abstract void cancel();
    public abstract int getQueueId();
    public abstract int getTaskId();
    public abstract int getTaskType();
    public void setTaskType(int taskType){
        this.taskType=taskType;
    }
    public void setNetworkManager(NetworkManager networkManager){
        this.networkManager=networkManager;
    }
    public void setNetworkTaskListener(NetworkManagerTaskListener listener){
        this.managerTaskListener =listener;
    }

    public boolean isUseBluetooth() {
        return useBluetooth;
    }

    public void setUseBluetooth(boolean useBluetooth) {
        this.useBluetooth = useBluetooth;
    }

    public boolean isUseHttp() {
        return useHttp;
    }

    public void setUseHttp(boolean useHttp) {
        this.useHttp = useHttp;
    }
}
