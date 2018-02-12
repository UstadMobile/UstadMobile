package com.ustadmobile.core.networkmanager;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h1>NetworkTask</h1>
 *
 * This is a class which define network task which might be AcquisitionTask or EntryStatusTask
 *
 *
 * @author kileha3
 */

public abstract class NetworkTask {

    private static final AtomicInteger taskIdAtomicInteger = new AtomicInteger();

    public NetworkTask(NetworkManagerCore networkManager) {
        this.networkManagerCore = networkManager;
        this.taskId = taskIdAtomicInteger.getAndIncrement();
    }

    public NetworkManagerTaskListener managerTaskListener;
    protected NetworkManagerCore networkManagerCore;
    public int queueId;
    public int taskId;
    public int taskType;
    public boolean useBluetooth;
    public boolean useHttp;

    private boolean stopped;

    public static final int STATUS_NOT_QUEUED = 0;

    //Waiting type statuses - 1-10

    /*
     * Statuses are grouped in ranges:
     * 0:       not queued
     * 1-10     waiting (inc. wait for retry)
     * 11-20    active/running
     * 21-30    finished
     */

    /**
     * The minimum value of waiting type statuses e.g. queued, waiting for connection, etc.
     */
    public static final int STATUS_WAITING_MIN = 1;

    /**
     * The maximum value of waiting type statuses - e.g.
     */
    public static final int STATUS_WAITING_MAX = 10;

    public static final int STATUS_QUEUED = 3;

    /**
     * Waiting for a connection. Could be waiting for Wifi / p2p availability if the download job
     * should complete without using mobile data. Could also be waiting for any kind of network if
     * the job does not depend on wifi.
     */
    public static final int STATUS_WAITING_FOR_CONNECTION = 4;

    public static final int STATUS_WAIT_FOR_RETRY = 5;

    //Running statuses - 11-20

    public static final int STATUS_RUNNING_MIN = 11;

    public static final int STATUS_RUNNING_MAX = 20;
    /**
     * The DownloadTask has been created and is starting. Done to ensure that there is no possibility
     * of two tasks being queued accidently at the same time.
     */
    public static final int STATUS_STARTING = 11;

    public static final int STATUS_RUNNING = 12;


    //Complete statuses where the job is not part of the queue - 21-30

    public static final int STATUS_COMPLETE_MIN = 20;

    public static final int STATUS_COMPLETE_MAX = 30;

    public static final int STATUS_COMPLETE = 24;

    public static final int STATUS_FAILED = 25;

    public static final int STATUS_STOPPED = 26;


    @Deprecated
    public static final int STATUS_WAITING = 1;

    @Deprecated
    public static final int STATUS_RETRY_LATER = 23;

    @Deprecated
    public static final int STATUS_WAITING_FOR_NETWORK = 27;

    private int status = STATUS_WAITING;


    /**
     * Method which initiate network task execution
     */
    public abstract void start();

    /**
     * Method which stop the network task execution
     */
    public synchronized void stop(int statusAfterStop) {
//        stopped = true;
//        setStatus(statusAfterStop);
//        UstadMobileSystemImpl.l(UMLog.INFO, 370, "NetworkTask #" + getTaskId() + " stopped");
    }

    protected synchronized void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public synchronized boolean isStopped() {
        return stopped;
    }


    /**
     * method which is used to get ID of the task on the task queue
     * @return int: Task queue id
     */
    public abstract int getQueueId();

    /**
     * Method which used to get task ID
     * @return int: Task ID
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Method which used to get TAsk type, it might be entry AcquisitionTask
     * or EntryStatusTask
     * @return int: Task type flag
     */
    public abstract int getTaskType();
    public void setTaskType(int taskType){
        this.taskType=taskType;
    }

    /**
     * Method which is used to set network manager to manage network operation on the task.
     * @param networkManager NetworkManager instance.
     */
    public void setNetworkManager(NetworkManagerCore networkManager){
        this.networkManagerCore = networkManager;
    }

    /**
     * Method which is use to set network task listener to the network task.
     * @param listener NetworkManagerTaskListener to fire events on right action.
     */
    public void setNetworkTaskListener(NetworkManagerTaskListener listener){
        this.managerTaskListener =listener;
    }

    /**
     * Method which is responsible to tell if the task is allowed to use bluetooth or not.
     * @return boolean: TRUE when allowed, FALSE otherwise.
     */
    public boolean isUseBluetooth() {
        return useBluetooth;
    }

    /**
     * Method which is used to tell the task to use or not use bluetooth in its operation.
     * @param useBluetooth TRUE when allowed, FALSE otherwise.
     */
    public void setUseBluetooth(boolean useBluetooth) {
        this.useBluetooth = useBluetooth;
    }

    /**
     * Method which is responsible to tell if the task is allowed to use HTTP connections or not.
     * @return boolean: TRUE when allowed, FALSE otherwise.
     */
    public boolean isUseHttp() {
        return useHttp;
    }

    /**
     * Method which is used to tell the task to use or not use HTTP connection in its operation.
     * @param useHttp TRUE when allowed, FALSE otherwise.
     */
    public void setUseHttp(boolean useHttp) {
        this.useHttp = useHttp;
    }

    protected synchronized void setStatus(int status) {
        this.status = status;
    }

    public synchronized int getStatus() {
        return this.status;
    }

    public boolean isFinished() {
        int status = getStatus();
        return (status == STATUS_COMPLETE || status == STATUS_FAILED);
    }

    public boolean isRetryNeeded() {
        return getStatus() == STATUS_RETRY_LATER;
    }

}
