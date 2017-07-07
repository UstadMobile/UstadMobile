package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.EntryCheckResponse;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkManagerTaskListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.port.sharedse.impl.http.CatalogUriResponder;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.MountedZipHandler;
import com.ustadmobile.port.sharedse.impl.http.OPDSFeedUriResponder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import static com.ustadmobile.core.buildconfig.CoreBuildConfig.NETWORK_SERVICE_NAME;

/**
 * <h1>NetworkManager</h1>
 *
 * This is the class which defines all cross platform. It is responsible to register all network listeners,
 * register all services, getting right device address like MAC address and IP address, handle bluetooth
 * and WiFi direct connections e.t.c
 *
 * @author kileha3
 *
 * @see NetworkManagerTaskListener
 * @see com.ustadmobile.core.networkmanager.NetworkManagerCore
 */

public abstract class NetworkManager implements NetworkManagerCore, NetworkManagerTaskListener, LocalMirrorFinder {

    /**
     * Flag to indicate queue type status
     */
    public static final int QUEUE_ENTRY_STATUS=0;

    /**
     * Flag to indicate queue type acquisition.
     */
    public static final int QUEUE_ENTRY_ACQUISITION=1;

    /**
     * Flag to indicate type of notification used when supernode is active
     */
    public static final int NOTIFICATION_TYPE_SERVER=0;

    /**
     * Flag to indicate type of notification used during file acquisition
     */
    public static final int NOTIFICATION_TYPE_ACQUISITION=1;

    /**
     * Flag to indicate file acquisition source is from cloud.
     */
    public static final int DOWNLOAD_FROM_CLOUD =1;

    /**
     * Flag to indicate file acquisition source is peer on the same network
     */
    public static final int DOWNLOAD_FROM_PEER_ON_SAME_NETWORK =2;

    /**
     * Flag to indicate file acquisition source is peer on different network
     */
    public static final int DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK =3;

    public BluetoothServer bluetoothServer;

    /**
     * Maximum time for the device to wait other peer services to be discovered.
     */
    public  static final int ALLOWABLE_DISCOVERY_RANGE_LIMIT =2 * 60 * 1000;

    /**
     * Tag to hold device IP address value in DNS-Text record
     */
    public static final String SD_TXT_KEY_IP_ADDR = "a";

    /**
     * Tag to hold device bluetooth address value in DNS-Text record
     */
    public static final String SD_TXT_KEY_BT_MAC = "b";

    /**
     * Tag to hold device service port value in DNS-Text record
     */
    public static final String SD_TXT_KEY_PORT = "port";

    /**
     * Flag to indicate wifi direct group is inactive and it is not under creation
     */
    public static final int WIFI_DIRECT_GROUP_STATUS_INACTIVE = 0;

    /**
     * Flag to indicate Wifi direct group is being created now
     */
    public static final int WIFI_DIRECT_GROUP_STATUS_UNDER_CREATION = 1;

    /**
     * Flag to indicate Wifi direct group is active
     */
    public static final int WIFI_DIRECT_GROUP_STATUS_ACTIVE = 2;

    private Object mContext;

    private Vector<NetworkNode> knownNetworkNodes=new Vector<>();

    private Vector<NetworkTask>[] tasksQueues = new Vector[] {
        new Vector<>(), new Vector<>()
    };


    private Vector<NetworkManagerListener> networkManagerListeners = new Vector<>();

    private Vector<AcquisitionListener> acquisitionListeners=new Vector<>();

    private Map<String,List<EntryCheckResponse>> entryResponses =new HashMap<>();

    //private NetworkTask[] currentTasks = new NetworkTask[2];

    private int[] currentTaskIndex = new int[]{-1, -1};

    private Vector<WiFiDirectGroupListener> wifiDirectGroupListeners = new Vector<>();

    private Map<String,AcquisitionTask> entryAcquisitionTaskMap=new HashMap<>();

    protected EmbeddedHTTPD httpd;

    public static final int AFTER_GROUP_CONNECTION_DO_NOTHING = 0;

    public static final int AFTER_GROUP_CONNECTION_DISCONNECT = 1;

    public static final int AFTER_GROUP_CONNECTION_RESTORE = 2;

    /**
     *
     */
    protected int actionRequiredAfterGroupConnection;

    /**
     * The prefix used on all Wifi Direct group networks as per the spec.
     */
    public static final String WIFI_DIRECT_GROUP_SSID_PREFIX = "DIRECT-";

    /**
     * The http prefix we use for the internal OPDS catalog server
     */
    public static final String CATALOG_HTTP_ENDPOINT_PREFIX = "/catalog/";


    /**
     * The uri used for an OPDS feed representing items the user wishes to share locally
     */
    public static final String CATALOG_HTTP_SHARED_URI = "/shared/shared.opds";

    /**
     * The ssid of the last "normal" wifi network connected to, if any
     */
    protected String ssidToRestore;

    /**
     * Used primarily for testing but cannot be conveniently pushed into test package.
     */
    private boolean mangleWifiSsid = false;

    private boolean mangleBluetoothAddr = false;

    public NetworkManager() {
    }

    /**
     * Method used to enable super node
     */
    public abstract void startSuperNode();

    /**
     * Method used to disable supper node
     */
    public abstract void stopSuperNode();


    /**
     * Run client services: discover nearby nodes
     */
    public abstract void startClientMode();

    /**
     * Stop client services
     */
    public abstract void stopClientMode();



    /**
     * Method used to check if super node is enabled.
     * @return boolean: TRUE if enabled and FALSE otherwise
     */
    public abstract boolean isSuperNodeEnabled();

    /**
     * Do the main initialization of the NetworkManager : set the context and start the http manager
     * This will have no effect if called twice
     *
     * @param mContext The context to use for the network manager
     */
    public synchronized void init(Object mContext) {
        if(this.mContext != null)
            return;

        this.mContext = mContext;

        try {
            httpd = new EmbeddedHTTPD(0);
            httpd.addRoute(CATALOG_HTTP_ENDPOINT_PREFIX + "(.)+", CatalogUriResponder.class, mContext, new WeakHashMap());
            NanoLrsHttpd.mountXapiEndpointsOnServer(httpd, mContext, "/xapi/");
            httpd.start();
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 1, "Failed to start http server");
            throw new RuntimeException("Failed to start http server", e);
        }
    }

    /**
     * Method which tell if bluetooth is enabled or disabled on the device.
     * @return boolean: TRUE, if bluetooth is enabled and FALSE otherwise
     */
    public  abstract boolean isBluetoothEnabled();

    /**
     * Enable or disable bluetooth. Depending on the underlying system and version this may or may
     * not be supported. Anything depending on bluetooth being disabled or enabled should be handled
     * in handleBluetoothEnabledChanged
     *
     * @return true the request was successfully submitted
     */
    public abstract boolean setBluetoothEnabled(boolean enabled);


    /**
     * Method which is used to get make refrence to the BluetoothServer
     * @return BluetoothServer
     */
    public abstract BluetoothServer getBluetoothServer();

    /**
     * Method to tell if Wi-Fi is enabled or disabled on the device
     * @return boolean: TRUE, if enabled otherwise FALSE.
     */
    public abstract boolean isWiFiEnabled();

    /**
     * Method to enable or disable WiFi.
     *
     * @param enabled
     * @return true if the operation is successful, false otherwise
     */
    public abstract boolean setWifiEnabled(boolean enabled);




    /**
     * Method which tells if the file can be downloaded locally or not.
     * @param entryId File Entry ID
     * @return boolean: TRUE, if is available locally otherwise FALSE.
     */
    public boolean isFileAvailable(String entryId){
        for(EntryCheckResponse response:entryResponses.get(entryId)){
            if(response.isFileAvailable()){
                return true;
            }
        }
        return false;
    }

    /**
     * Request the status of given entryIds to see if they are available locally or not
     *
     * @param entryIds EntryIDs (e.g. as per an OPDS catalog) to look for
     * @param mContext System context
     * @param nodeList List of all peer nodes discovered
     * @param useBluetooth If true - use bluetooth addresses that were discovered using WiFi direct to ask for availability
     * @param useHttp If true - use HTTP to talk with nodes discovered which are reachable using HTTP (e.g. nodes already connected to the same wifi network)
     *
     * @return
     */
    public long requestFileStatus(List<String> entryIds,Object mContext,List<NetworkNode> nodeList, boolean useBluetooth, boolean useHttp){
        EntryStatusTask task = new EntryStatusTask(entryIds,nodeList,this);
        task.setTaskType(QUEUE_ENTRY_STATUS);
        task.setUseBluetooth(useBluetooth);
        task.setUseHttp(useHttp);
        queueTask(task);
        return task.getTaskId();
    }

    public long requestFileStatus(String[] entryIds, boolean useBluetooth, boolean useHttp) {
        return requestFileStatus(Arrays.asList(entryIds), getContext(), getKnownNodes(), useBluetooth, useHttp);
    }


    /**
     * Request the status of given entryIds to see if they are available locally or not. By default
     * use both bluetooth and http
     *
     * @param entryIds EntryIDs (e.g. as per an OPDS catalog) to look for
     * @param mContext System context
     * @param nodeList
     *
     * @return
     */
    public long requestFileStatus(List<String> entryIds,Object mContext,List<NetworkNode> nodeList) {
        return requestFileStatus(entryIds, mContext, nodeList, true, true);
    }

    /**
     * Method which invoked when making file acquisition request.
     * @param feed OPDS file feed
     * @param localNetworkEnabled Whether to involve local network as means of acquiring,
     *                            TRUE if yes, FALSE otherwise.
     * @param mirrorFinder Normally the NetworkManager itself but can be any other object implementing
     *                     the interface (e.g. for testing purposes)
     * @param wifiDirectEnabled Whether to involve Wi-Fi direct group as means of acquiring,
     *                          TRUE if yes , otherwise FALSE.
     * @return
     */
    public long requestAcquisition(UstadJSOPDSFeed feed, LocalMirrorFinder mirrorFinder,
                                              boolean localNetworkEnabled, boolean wifiDirectEnabled){
        AcquisitionTask task=new AcquisitionTask(feed,this);
        task.setMirrorFinder(mirrorFinder);
        task.setTaskType(QUEUE_ENTRY_ACQUISITION);
        task.setLocalNetworkDownloadEnabled(localNetworkEnabled);
        task.setWifiDirectDownloadEnabled(wifiDirectEnabled);
        queueTask(task);
        return task.getTaskId();
    }

    public long requestAcquisition(UstadJSOPDSFeed feed, boolean localNetworkEnabled, boolean wifiDirectEnabled){
        return requestAcquisition(feed, this, localNetworkEnabled, wifiDirectEnabled);
    }

    /**
     * Creating task que as per request received.
     * @param task Network task to be queued
     * @return NetworkTask: Queued NetworkTask
     */
    public NetworkTask queueTask(NetworkTask task){
        tasksQueues[task.getTaskType()].addElement(task);
        checkTaskQueue(task.getTaskType());

        return task;
    }

    /**
     * Method which check the queue and manage it,
     * if there is any task to be executed will be executed.
     *
     * @param queueType Queue type, whether is queue of Acquisition task or EntryStatus task
     */
    public synchronized void checkTaskQueue(int queueType){
        if(queueType == QUEUE_ENTRY_ACQUISITION && tasksQueues[queueType].isEmpty()) {
            switch(actionRequiredAfterGroupConnection) {
                case AFTER_GROUP_CONNECTION_DISCONNECT:
                    UstadMobileSystemImpl.l(UMLog.INFO, 326,
                        "NetworkManager:checkTaskQueue: all tasks complete - WiFi to be disconnected after group connection");
                    actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DO_NOTHING;
                    disconnectWifi();
                    break;

                case AFTER_GROUP_CONNECTION_RESTORE:
                    UstadMobileSystemImpl.l(UMLog.INFO, 327,
                        "NetworkManager:checkTaskQueue: all tasks complete - WiFi to be restored to 'normal' after group connection");
                    actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DO_NOTHING;
                    restoreWifi();
                    break;
            }
        }


        if(currentTaskIndex[queueType] == -1) {
            int nextTaskIndex = selectNextTask(tasksQueues[queueType]);

            if(nextTaskIndex != -1) {
                currentTaskIndex[queueType] = nextTaskIndex;
                NetworkTask currentTask = tasksQueues[queueType].get(currentTaskIndex[queueType]);
                currentTask.setNetworkManager(this);
                currentTask.setNetworkTaskListener(this);
                currentTask.start();
            }

        }
    }

    /**
     * Finds the next task to do as follows:
     *
     * 1. Skip any task that has been manually stopped
     * TODO: handle a task that has a do not start before time on it
     *
     * @param tasks
     *
     * @return index of the next task to run
     */
    protected int selectNextTask(Vector<NetworkTask> tasks) {
        for(int i = 0; i < tasks.size(); i++){
            if(tasks.get(i).getStatus() != NetworkTask.STATUS_STOPPED)
                return i;
        }

        return -1;
    }


    /**
     * Method which is invoked when new node has been found from Wi-Fi Direct discovery service.
     * @param serviceFullDomain Combination of application service record and protocol used
     * @param senderMacAddr Host device MAC address
     * @param txtRecords Map of DNS-Text records
     */
    public void handleWifiDirectSdTxtRecordsAvailable(String serviceFullDomain,String senderMacAddr, HashMap<String, String> txtRecords) {
        if(serviceFullDomain.contains(NETWORK_SERVICE_NAME)){
            String ipAddr = txtRecords.get(SD_TXT_KEY_IP_ADDR);
            String btAddr = txtRecords.get(SD_TXT_KEY_BT_MAC);
            int port=Integer.parseInt(txtRecords.get(SD_TXT_KEY_PORT));

            boolean newNode;
            NetworkNode node = null;
            synchronized (knownNetworkNodes) {
                newNode = true;
                if(ipAddr != null) {
                    node = getNodeByIpAddress(ipAddr);
                    newNode = (node == null);
                }


                if(node == null) {
                    node = new NetworkNode(senderMacAddr,ipAddr);
                    node.setDeviceIpAddress(ipAddr);
                    knownNetworkNodes.add(node);
                }

                node.setDeviceBluetoothMacAddress(btAddr);
                node.setDeviceWifiDirectMacAddress(senderMacAddr);
                node.setPort(port);
                node.setWifiDirectLastUpdated(Calendar.getInstance().getTimeInMillis());
            }


            if(newNode){
                fireNetworkNodeDiscovered(node);
            }else{
                fireNetworkNodeUpdated(node);
            }

        }
    }

    /**
     * Method which will be invoked when new node is found using network service discovery
     * @param serviceName application service name
     * @param ipAddress Host device IP address
     * @param port Service port on host device
     */
    public void handleNetworkServerDiscovered(String serviceName,String ipAddress,int port){
        if(serviceName.contains(NETWORK_SERVICE_NAME)){
            NetworkNode node = null;
            boolean newNode = false;
            synchronized (knownNetworkNodes) {
                newNode = true;
                if(ipAddress != null) {
                    node = getNodeByIpAddress(ipAddress);
                    newNode = (node == null);
                }


                if(node == null) {
                    node = new NetworkNode(null,ipAddress);
                    knownNetworkNodes.add(node);
                }

                node.setNetworkServiceLastUpdated(Calendar.getInstance().getTimeInMillis());
                node.setPort(port);
            }


            if(newNode){
                fireNetworkNodeDiscovered(node);
            }else{
                fireNetworkNodeUpdated(node);
            }
        }
    }

    /**
     * This method must be called by the underlying implementation when this is detected.
     *
     * @param enabled
     */
    public void handleWifiEnabledChanged(boolean enabled) {
        if(enabled) {
            checkTaskQueue(QUEUE_ENTRY_ACQUISITION);
            if(isSuperNodeEnabled() && isBluetoothEnabled()) {
                startSuperNode();
            }else if(!isSuperNodeEnabled()){
                startClientMode();
            }
        }else {
            stopClientMode();
            stopSuperNode();

            if(currentTaskIndex[QUEUE_ENTRY_ACQUISITION] != -1) {
                tasksQueues[QUEUE_ENTRY_ACQUISITION].get(currentTaskIndex[QUEUE_ENTRY_ACQUISITION]).stop(
                        NetworkTask.STATUS_WAITING_FOR_NETWORK);
                currentTaskIndex[QUEUE_ENTRY_ACQUISITION] = -1;
            }
        }

    }

    /**
     * This method must be called by the underlying implementation when the change is detected.
     * @param enabled
     */
    public void handleBluetoothEnabledChanged(boolean enabled) {

    }


    /**
     * Get known network node using it's IP address
     * @param ipAddr Node's IP address to search for.
     * @return NetworkNode object
     */
    public NetworkNode getNodeByIpAddress(String ipAddr) {
        synchronized (knownNetworkNodes) {
            String nodeIp;
            for(NetworkNode node : knownNetworkNodes) {
                nodeIp = node.getDeviceIpAddress();
                if(nodeIp != null && nodeIp.equals(ipAddr))
                    return node;
            }
        }

        return null;
    }

    /**
     * Get known network node using it's bluetooth address
     * @param bluetoothAddr Node's bluetooth address to search for.
     * @return NetworkNode object
     */
    public NetworkNode getNodeByBluetoothAddr(String bluetoothAddr) {
        synchronized (knownNetworkNodes) {
            String nodeBtAddr;
            for(NetworkNode node : knownNetworkNodes) {
                nodeBtAddr = node.getDeviceBluetoothMacAddress();
                if(nodeBtAddr != null && nodeBtAddr.equals(bluetoothAddr))
                    return node;
            }
        }

        return null;
    }

    /**
     * Method which used to register NetworkManagerListener to listen for Network events.
     * @param listener NetworkManagerListener instance.
     */
    public void addNetworkManagerListener(NetworkManagerListener listener){
        networkManagerListeners.add(listener);
    }

    /**
     * Method which used to remove NetworkManagerListener after being registered.
     * @param listener NetworkManagerListener to be removed
     */
    public void removeNetworkManagerListener(NetworkManagerListener listener){
        if(listener!=null){
            networkManagerListeners.remove(listener);
        }
    }

    /**
     * Method invoked when device request bluetooth connection.
     * @param deviceAddress Peer device bluetooth address to connect to.
     * @param handler BluetoothConnectionHandler which listen for connection events.
     */
    public abstract void connectBluetooth(String deviceAddress,BluetoothConnectionHandler handler);

    /**
     * Method which invoked when entry status responses is are received
     * @param node NetworkNode on which entry status check task was executed on.
     * @param fileIds List of all entries
     * @param status List of all entries status
     */
    public void handleEntriesStatusUpdate(NetworkNode node, List<String> fileIds,List<Boolean> status) {
        List<EntryCheckResponse> responseList;
        EntryCheckResponse checkResponse;
        long timeNow = Calendar.getInstance().getTimeInMillis();
        for (int position=0;position<fileIds.size();position++){
            checkResponse = getEntryResponse(fileIds.get(position), node);

            responseList=getEntryResponses().get(fileIds.get(position));
            if(responseList==null){
                responseList=new ArrayList<>();
                entryResponses.put(fileIds.get(position),responseList);
            }

            if(checkResponse == null) {
                checkResponse = new EntryCheckResponse(node);
                responseList.add(checkResponse);
            }

            checkResponse.setFileAvailable(status.get(position));
            checkResponse.setLastChecked(timeNow);
        }

        fireFileStatusCheckInformationAvailable(fileIds);
    }

    /**
     * Method which get particular entry status response on a specific node from list of responses.
     * @param fileId Entry Id to look for the status
     * @param node NetworkNode to request response from.
     * @return EntryCheckResponse object
     */

    public EntryCheckResponse getEntryResponse(String fileId, NetworkNode node) {
        List<EntryCheckResponse> responseList = getEntryResponses().get(fileId);
        if(responseList == null)
            return null;

        for(int responseNum = 0; responseNum < responseList.size(); responseNum++) {
            if(responseList.get(responseNum).getNetworkNode().equals(node)) {
                return responseList.get(responseNum);
            }
        }

        return null;
    }

    /**
     * Get response from response list which contains a file we a looking for and can be downloaded locally,
     * first priority is given to node on the same network.
     * If no matching node then check for the node on different network.
     * @param entryId
     * @return
     */
    @Override
    public List<EntryCheckResponse> getEntryResponsesWithLocalFile(String entryId){
        List<EntryCheckResponse> responseList=getEntryResponses().get(entryId);
        return responseList;
    }

    /**
     * Method which will be called to fire all events when file acquisition information is available
     * @param entryId Entry Id under processing
     * @param downloadId Id assigned to the acquisition task
     * @param downloadSource File source, which might be from cloud,
     *                       peer on the same network or peer on different network
     */
    public void handleFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource){
        fireFileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
    }

    /**
     * Method which will be called to fire all Wi-Fi Direct connection events
     * @param ssid SSID of the current connected Wi-Fi Direct group.
     */
    public void handleWifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting){
        if(ssid != null && connected && !ssid.toUpperCase().startsWith("DIRECT-")) {
            //this is not a wifi direct network
            actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DO_NOTHING;
        }

        fireWiFiConnectionChanged(ssid, connected, connectedOrConnecting);
    }

    /**
     * Method which will be called to fire all events when entry status check
     * is completed and information is there to process
     * @param fileIds List of entry ID's which were processed
     */
    protected void fireFileStatusCheckInformationAvailable(List<String> fileIds) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileStatusCheckInformationAvailable(fileIds);
            }
        }
    }

    /**
     * Method firing event to all listening part of the app
     * when file acquisition information is available.
     * @param entryId Entry Id under processing
     * @param downloadId Id assigned to the acquisition task
     * @param downloadSource File source, which might be from cloud,
     *                       peer on the same network or peer on different network
     */
    private void fireFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
            }
        }
    }

    /**
     * Method which fire event to all listening part of the
     * app when new node has been found.
     * @param node NetworkNode object which contain all node information
     */
    protected void fireNetworkNodeDiscovered(NetworkNode node) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkNodeDiscovered(node);
            }
        }
    }

    /**
     * Method which will be firing events to notify other part of the app that node
     * information has been updated.
     * @param node NetworkNode object
     */
    protected void fireNetworkNodeUpdated(NetworkNode node){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkNodeUpdated(node);
            }
        }
    }

    /**
     * Method which will be firing events to notify the listening parts of the app that the
     * entry status check task has been completed.
     * @param task Entry status check NetworkTask
     */
    protected void fireNetworkTaskStatusChanged(NetworkTask task){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkTaskStatusChanged(task);
            }
        }
    }

    /**
     * Method which will be firing events to all listening part ot the app to notify
     * that Wi-Fi state connection has been changed.
     * @param ssid Currently connected Wi-Fi network SSID
     */
    protected void fireWiFiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.wifiConnectionChanged(ssid, connected, connectedOrConnecting);
            }
        }
    }

    /**
     * Method which will be used to add notification as user feedback when super node is
     * activated or during entry acquisition execution
     * @param notificationType Type of notification (File acquisition or Super node notification)
     * @param title Notification title
     * @param message Notification message which provide more information about the notification
     * @return int: Type of notification
     */
    public abstract int addNotification(int notificationType,String title,String message);

    /**
     * Method which is responsible for updating the notification information especially
     * when the notification type is Acquisition
     * @param notificationType Type of notification (File acquisition or Super node notification)
     * @param progress File acquisition progress status
     * @param title Notification title
     * @param message Notification message which provide more information about the notification
     */
    public abstract void updateNotification(int notificationType,int progress,String title,String message);

    /**
     * Method which will be responsible for removing notification when file acquisition task
     * is completed or Super node mode was deactivated.
     * @param notificationType Type of notification to be removed (File acquisition or Super node notification)
     */
    public abstract void removeNotification(int notificationType);

    @Override
    public void networkTaskStatusChanged(NetworkTask task) {
        int taskType = task.getTaskType();
        if(currentTaskIndex[taskType] != -1 && task.getTaskId() == tasksQueues[taskType].get(currentTaskIndex[taskType]).getTaskId()){
            int status = task.getStatus();
            switch(status) {
                case NetworkTask.STATUS_COMPLETE:
                case NetworkTask.STATUS_FAILED:
                    //task is finished
                    tasksQueues[taskType].remove(currentTaskIndex[taskType]);
                    currentTaskIndex[taskType] = -1;
                    checkTaskQueue(taskType);
                    break;
                case NetworkTask.STATUS_RETRY_LATER:
                case NetworkTask.STATUS_STOPPED:
                    //put task to back of queue
                    NetworkTask retryTask = tasksQueues[taskType].remove(currentTaskIndex[taskType]);
                    tasksQueues[taskType].addElement(retryTask);
                    currentTaskIndex[taskType] = -1;
                    checkTaskQueue(taskType);
                    break;
            }
        }

        fireNetworkTaskStatusChanged(task);
    }

    public List<NetworkNode> getKnownNodes() {
        return knownNetworkNodes;
    }

    public Map<String,List<EntryCheckResponse>> getEntryResponses(){
        return entryResponses;
    }

    public Object getContext() {
        return mContext;
    }

    /**
     * Getting the current device IP address from the current connected network.
     * @return String: Current device IP address
     */
    public abstract String getDeviceIPAddress();


    public void handleWifiDirectGroupCreated(WiFiDirectGroup wiFiDirectGroup){
        fireWifiDirectGroupCreated(wiFiDirectGroup,null);
    }

    public void handleWifiDirectGroupRemoved(boolean isGroupRemoved){
        fireWifiDirectGroupRemoved(isGroupRemoved,null);
    }


    /**
     * Connect to the given wifi network. Use NetworkManagerListener.wifiConnectionChanged to
     * listen for a successful connection.  There is not necessarily any event for a failed connection
     * : tasks using this may have to use a timeout approach to detect failure.
     *
     * @param ssid SSID to connect to
     * @param passphrase Passphrase to use
     */
    public abstract void connectWifi(String ssid,String passphrase);

    /**
     * Called to "restore" the wifi connection to the 'normal' WiFi. Used when we have connected
     * to a WiFi direct group to restore the connection to the normal WiFi.
     */
    public abstract void restoreWifi();

    /**
     * Called
     */
    public abstract void disconnectWifi();

    /**
     * WARNING: This method is really here for testing purposes only. When enabled any call to the
     * getWifiDirectGroup method will provide an invalid ssid. The actual ssid will be postfixed
     * with "-mangle". Unfortunately putting this in the test package would require overriding
     * UstadMobileSystemImplFactory
     *
     * @hide
     * @param mangleWifiSsid
     */
    public void setMangleWifiDirectGroup(boolean mangleWifiSsid) {
        this.mangleWifiSsid = mangleWifiSsid;
    }

    /**
     * @hide
     * @return
     */
    protected boolean isMangleWifiDirectGroup() {
        return this.mangleWifiSsid;
    }


    public void setMangleBluetoothAddr(boolean mangleBluetoothAddr) {
        this.mangleBluetoothAddr = mangleBluetoothAddr;
    }

    protected boolean isMangleBluetoothAddr() {
        return mangleBluetoothAddr;
    }


    /**
     * Connect to the given ssid. This method will mark the connection as a temporary connection.
     *
     * This method will
     *  1. Check the current connectivity status and set a flag for what to do once we are finished
     *     with this connection (either disconnect if there is currently no active wifi network,
     *     or restore if there is an active wifi connection)
     *
     *  2. Connect to the given network
     *
     * @param ssid
     * @param passphrase
     */
    public void connectToWifiDirectGroup(String ssid, String passphrase){
        if(actionRequiredAfterGroupConnection == AFTER_GROUP_CONNECTION_DO_NOTHING){
            if(getCurrentWifiSsid() != null) {
                actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_RESTORE;
            }else {
                actionRequiredAfterGroupConnection = AFTER_GROUP_CONNECTION_DISCONNECT;
            }
        }

        connectWifi(ssid, passphrase);
    }

    /**
     * Determine if the device is currently connected to a wifi direct network or not
     * @return
     */
    public boolean isConnectedToWifiDirectGroup() {
        String ssid = getCurrentWifiSsid();
        return ssid != null && ssid.toUpperCase().startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX);
    }

    /**
     * Gets the currently connected wifi ssid : if any
     *
     * @return
     */
    public abstract String getCurrentWifiSsid();



    /**
     * Create a new WiFi direct group on this device. A WiFi direct group
     * will create a new SSID and passphrase other devices can use to connect in "legacy" mode.
     *
     * The process is asynchronous and the WifiDirectGroupListener should be used to listen for
     * group creation.
     *
     * If a WiFi direct group is already under creation this method has no effect.
     */
    public abstract void createWifiDirectGroup();


    /**
     * Stop the WiFi direct group if it is active. If there is no group this method will have no effect.
     */
    public abstract void removeWiFiDirectGroup();


    /**
     * Gets the current active WiFi Direct group (if any)
     *
     * @return The active WiFi direct group (if any) - otherwise null
     */
    public abstract WiFiDirectGroup getWifiDirectGroup();



    /**
     * Add a WiFiDirectGroupListener to receive notifications for group
     * creation and removal
     *
     * @param listener Listener to add
     */
    public void addWifiDirectGroupListener(WiFiDirectGroupListener listener) {
        wifiDirectGroupListeners.add(listener);
    }

    /**
     * Remove a given WifiDirectGroupListener
     *
     * @param listener Listener to remove
     */
    public void removeWifiDirectGroupListener(WiFiDirectGroupListener listener) {
        wifiDirectGroupListeners.remove(listener);
    }

    /**
     * Method which will be firing event to all listening part of the app upon successful Wi-Fi Direct group creation.
     * @param group WiFiDirectGroup wrapper  with more information about the group
     * @param error Exception thrown.
     */
    protected void fireWifiDirectGroupCreated(WiFiDirectGroup group, Exception error) {
        synchronized (wifiDirectGroupListeners) {
            for(int i = 0; i < wifiDirectGroupListeners.size(); i++) {
                wifiDirectGroupListeners.get(i).groupCreated(group, error);
            }
        }
    }

    /**
     * Method which will be firing event to all listening part of the app upon successful removal of the Wi-Fi Direct group
     * @param successful removal status (If removed TRUE, otherwise FALSE)
     * @param error
     */
    protected void fireWifiDirectGroupRemoved(boolean successful, Exception error) {
        synchronized (wifiDirectGroupListeners) {
            for(int i = 0; i < wifiDirectGroupListeners.size(); i++) {
                wifiDirectGroupListeners.get(i).groupRemoved(successful, error);
            }
        }
    }


    /**
     * Method which is responsible for adding all acquisition listeners.
     * @param listener AcquisitionListener to listen to and fire events accordingly
     */
    public void addAcquisitionTaskListener(AcquisitionListener listener){
        acquisitionListeners.add(listener);
    }

    /**
     * Method which is responsible for removing all listeners added
     * @param listener
     */
    public void removeAcquisitionTaskListener(AcquisitionListener listener){
        acquisitionListeners.remove(listener);
    }

    /**
     * Fire acquisition progress updates to the listening part of the app
     * @param entryId
     */
    protected void fireAcquisitionProgressUpdate(String entryId, AcquisitionTask task){
        synchronized (acquisitionListeners) {
            for(AcquisitionListener listener : acquisitionListeners){
                listener.acquisitionProgressUpdate(entryId, task.getStatusByEntryId(entryId));
            }
        }
    }


    /**
     * Fire acquisition status change to all listening parts of the app
     * @param entryId
     */
    protected void fireAcquisitionStatusChanged(String entryId, AcquisitionTask.Status status){
        UstadMobileSystemImpl.l(UMLog.DEBUG, 645, "fireAcquisitionStatusChanged: " + entryId +
                " : " + status.getStatus());
        synchronized (acquisitionListeners) {
            for(AcquisitionListener listener : acquisitionListeners){
                listener.acquisitionStatusChanged(entryId, status);
            }
        }
    }

    /**
     * Find the acquisition task for the given entry id
     *
     * @param entryId Entry ID to find
     *
     * @return The task carrying out acquisition of this entry, or null if it's not being acquired by any known task
     */
    public AcquisitionTask getAcquisitionTaskByEntryId(String entryId) {
        synchronized (tasksQueues[QUEUE_ENTRY_ACQUISITION]) {
            AcquisitionTask acquisitionTask;
            for(int i = 0; i < tasksQueues[QUEUE_ENTRY_ACQUISITION].size(); i++) {
                acquisitionTask = (AcquisitionTask)tasksQueues[QUEUE_ENTRY_ACQUISITION].get(i);
                if(acquisitionTask.taskIncludesEntry(entryId))
                    return acquisitionTask;
            }
        }

        return null;
    }

    /**
     * Get a network task by id and queue type
     *
     * @param taskId The task id
     * @param queueType
     *
     * @return
     */
    public NetworkTask getNetworkTaskByTaskId(long taskId, int queueType) {
        synchronized (tasksQueues[queueType]) {
            for(int i = 0; i < tasksQueues[queueType].size(); i++) {
                if(tasksQueues[queueType].get(i).getTaskId() == taskId) {
                    return tasksQueues[queueType].get(i);
                }
            }
        }

        return null;
    }


    /**
     * Return the Entry ID to AcquisitionTask map
     * @return
     */
    public Map<String,AcquisitionTask> getEntryAcquisitionTaskMap(){
        return entryAcquisitionTaskMap;
    }


    /**
     * Returns the IP address of this device as used on Wifi Direct connections.
     *
     * @return The Wifi Direct IP address, or null if none
     */
    public abstract String getWifiDirectIpAddress();

    /**
     * Gets the current status of the Wifi direct group.  Will return
     * one of the WIFIDIRECT_GROUP_STATUS_  constants
     *
     * @return Wifi direct group status as per the constants
     */
    public abstract int getWifiDirectGroupStatus();

    /**
     * Clean up the network manager for shutdown
     */
    public void onDestroy() {
        if(httpd != null) {
            httpd.stop();
        }
    }

    /**
     * Mount a Zip File to the http server.  Optionally specify a preferred mount point (useful if
     * the activity is being created from a saved state)
     *
     * @param zipPath Path to the zip that should be mounted (mandatory)
     * @param mountName Directory name that this should be mounted as e.g. something.epub-timestamp
     *
     * @return The mountname that was used - the ocntent will then be accessible on getZipMountURL()/return value
     */
    public String mountZipOnHttp(String zipPath, String mountName) {
        UstadMobileSystemImpl.l(UMLog.INFO, 371, "Mount zip " + zipPath + " on service "
                + this + "httpd server = " + httpd);
        String extension = UMFileUtil.getExtension(zipPath);
        HashMap<String, List<MountedZipHandler.MountedZipFilter>> filterMap = null;

        if(extension != null && extension.endsWith("epub")) {
            filterMap = new HashMap<>();
            List<MountedZipHandler.MountedZipFilter> xhtmlFilterList = new ArrayList<>();
            MountedZipHandler.MountedZipFilter autoplayFilter = new MountedZipHandler.MountedZipFilter(
                    Pattern.compile("autoplay(\\s?)=(\\s?)([\"'])autoplay", Pattern.CASE_INSENSITIVE),
                    "data-autoplay$1=$2$3autoplay");
            xhtmlFilterList.add(autoplayFilter);
            filterMap.put("xhtml", xhtmlFilterList);
        }

        mountName = httpd.mountZip(zipPath, mountName, filterMap);
        return mountName;
    }

    /**
     * Method which is responsible for unmounting zipped file from HTTP
     * @param mountName File mount name
     */
    public void unmountZipFromHttp(String mountName) {
        httpd.unmountZip(mountName);
    }

    /**
     * Method which is used to get HTTP service listening port.
     * @return int: Listening port
     */
    public int getHttpListeningPort() {
        return httpd.getListeningPort();
    }

    /**
     * Get the local HTTP server url with the URL as it is to be used for access over the loopback
     * interface
     *
     * @return Local http server url e.g. http://127.0.0.1:PORT/
     */
    public String getLocalHttpUrl() {
        return "http://127.0.0.1:" + getHttpListeningPort() + "/";
    }

    /**
     * Method which is responsible for sharing application setup file to other devices.
     * @param filePath Setup file absolute path
     * @param shareTitle Share dialog title
     */
    public abstract void shareAppSetupFile(String filePath, String shareTitle);

    /**
     * Sets the shared http endpoint catalog.
     *
     * @param sharedFeed
     */
    public void setSharedFeed(UstadJSOPDSFeed sharedFeed) {
        httpd.addRoute(CATALOG_HTTP_SHARED_URI, OPDSFeedUriResponder.class, sharedFeed);
    }

    /**
     * Set the shared http endpoint catalog using an array of entry ids to be shared
     *
     * @param entryIds
     */
    public void setSharedFeed(String[] entryIds) {
        String feedSrcHref = UMFileUtil.joinPaths(new String[] {getLocalHttpUrl(),
                "/shared/shared.opds"});
        UstadJSOPDSFeed feed = new UstadJSOPDSFeed(feedSrcHref, "Shared courses",
                UMUUID.randomUUID().toString());
        UstadJSOPDSEntry entry;
        CatalogEntryInfo entryInfo;
        for(int i = 0; i < entryIds.length; i++) {
            entryInfo = CatalogController.getEntryInfo(entryIds[i], CatalogController.SHARED_RESOURCE,
                    getContext());

            if(entryInfo == null || entryInfo.acquisitionStatus != CatalogController.STATUS_ACQUIRED)
                continue;//cannot be shared if it has not been acquired.

            entry = new UstadJSOPDSEntry(feed, "Shared: " + entryInfo.fileURI, entryIds[i],
                    UstadJSOPDSFeed.LINK_ACQUIRE, entryInfo.mimeType, CATALOG_HTTP_ENDPOINT_PREFIX);
            feed.addEntry(entry);
        }

        setSharedFeed(feed);
    }

    /**
     * Reset the acquisition history of all known nodes. History is used to track failures so that
     * failure prone nodes are avoided as far as possible.
     *
     */
    public void clearNetworkNodeAcquisitionHistory() {
        Iterator<NetworkNode> nodeIterator = getKnownNodes().iterator();
        NetworkNode node;
        while(nodeIterator.hasNext()) {
            node = nodeIterator.next();
            if(node.getAcquisitionHistory() != null)
                node.getAcquisitionHistory().clear();
        }
    }


}
