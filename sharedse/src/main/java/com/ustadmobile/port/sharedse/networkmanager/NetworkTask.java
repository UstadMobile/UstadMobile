package com.ustadmobile.port.sharedse.networkmanager;

/**
 * Created by kileha3 on 08/05/2017.
 */

public abstract class NetworkTask {

    public NetworkManagerTaskListener managerTaskListener;
    public NetworkManager networkManager;
    public int queueId;
    public int taskId;
    public int taskType;

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



}
