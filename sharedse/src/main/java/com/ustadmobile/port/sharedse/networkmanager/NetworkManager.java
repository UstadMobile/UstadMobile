package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.port.sharedse.impl.http.CatalogUriResponder;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.MountedZipHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import static com.ustadmobile.core.buildconfig.CoreBuildConfig.NETWORK_SERVICE_NAME;

/**
 * Created by kileha3 on 08/05/2017.
 */

public abstract class NetworkManager implements NetworkManagerCore,NetworkManagerTaskListener {

    public static final int QUEUE_ENTRY_STATUS=0;
    public static final int QUEUE_ENTRY_ACQUISITION=1;
    public static final int NOTIFICATION_TYPE_SERVER=0;
    public static final int NOTIFICATION_TYPE_ACQUISITION=1;
    public static final int DOWNLOAD_FROM_CLOUD =1;
    public static final int DOWNLOAD_FROM_PEER_ON_SAME_NETWORK =2;
    public static final int DOWNLOAD_FROM_PEER_ON_DIFFERENT_NETWORK =3;

    public BluetoothServer bluetoothServer;
    public  static final int ALLOWABLE_DISCOVERY_RANGE_LIMIT =2 * 60 * 1000;

    public static final String SD_TXT_KEY_IP_ADDR = "a";

    public static final String SD_TXT_KEY_BT_MAC = "b";

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

    private NetworkTask[] currentTasks = new NetworkTask[2];

    private Vector<WiFiDirectGroupListener> wifiDirectGroupListeners = new Vector<>();

    private Map<String,AcquisitionTask> entryAcquisitionTaskMap=new HashMap<>();

    protected EmbeddedHTTPD httpd;

    public NetworkManager() {
    }

    public abstract void startSuperNode();

    public abstract void stopSuperNode();

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
            httpd.addRoute("/catalog/(.)+", CatalogUriResponder.class, mContext, new WeakHashMap());
            NanoLrsHttpd.mountXapiEndpointsOnServer(httpd, mContext, "/xapi/");
            httpd.start();
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 1, "Failed to start http server");
            throw new RuntimeException("Failed to start http server", e);
        }
    }

    public  abstract boolean isBluetoothEnabled();


    public abstract BluetoothServer getBluetoothServer();

    public abstract boolean isWiFiEnabled();

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
     * @param nodeList
     * @param useBluetooth If true - use bluetooth addresses that were discovered using WiFi direct to ask for availability
     * @param useHttp If true - use HTTP to talk with nodes discovered which are reachable using HTTP (e.g. nodes already connected to the same wifi network)
     *
     * @return
     */
    public List<String> requestFileStatus(List<String> entryIds,Object mContext,List<NetworkNode> nodeList, boolean useBluetooth, boolean useHttp){
        EntryStatusTask task = new EntryStatusTask(entryIds,nodeList,this);
        task.setTaskType(QUEUE_ENTRY_STATUS);
        task.setUseBluetooth(useBluetooth);
        task.setUseHttp(useHttp);
        queueTask(task);
        return entryIds;
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
    public List<String> requestFileStatus(List<String> entryIds,Object mContext,List<NetworkNode> nodeList) {
        return requestFileStatus(entryIds, mContext, nodeList, true, true);
    }

    public UstadJSOPDSFeed requestAcquisition(UstadJSOPDSFeed feed,Object mContext,
                                              boolean localNetworkEnabled, boolean wifiDirectEnabled){
        AcquisitionTask task=new AcquisitionTask(feed,this);
        task.setTaskType(QUEUE_ENTRY_ACQUISITION);
        task.setLocalNetworkDownloadEnabled(localNetworkEnabled);
        task.setWifiDirectDownloadEnabled(wifiDirectEnabled);
        queueTask(task);
        return feed;
    }


    public NetworkTask queueTask(NetworkTask task){
        tasksQueues[task.getTaskType()].add(task);
        checkTaskQueue(task.getTaskType());

        return task;
    }

    public synchronized void checkTaskQueue(int queueType){
        if(!tasksQueues[queueType].isEmpty() && currentTasks[queueType] == null) {
            currentTasks[queueType] = tasksQueues[queueType].remove(0);
            currentTasks[queueType].setNetworkManager(this);
            currentTasks[queueType].setNetworkTaskListener(this);
            currentTasks[queueType].start();
        }
    }

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
     * Get a known network node by IP address
     *
     * @param ipAddr
     * @return
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

    public void addNetworkManagerListener(NetworkManagerListener listener){
        networkManagerListeners.add(listener);
    }

    public void removeNetworkManagerListener(NetworkManagerListener listener){
        if(listener!=null){
            networkManagerListeners.remove(listener);
        }
    }

    public abstract void connectBluetooth(String deviceAddress,BluetoothConnectionHandler handler);

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
    public EntryCheckResponse getEntryResponseWithLocalFile(String entryId){
        List<EntryCheckResponse> responseList=getEntryResponses().get(entryId);
        if(responseList!=null &&!responseList.isEmpty()){
            for(EntryCheckResponse response: responseList){
                if(response.isFileAvailable() && Calendar.getInstance().getTimeInMillis() - response.getNetworkNode().getNetworkServiceLastUpdated() < ALLOWABLE_DISCOVERY_RANGE_LIMIT){
                    return response;
                }else{
                    if(response.isFileAvailable() && response.getNetworkNode().getWifiDirectLastUpdated()
                            < ALLOWABLE_DISCOVERY_RANGE_LIMIT){
                        return response;
                    }
                }
            }
        }
        return null;
    }

    public void handleFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource){
        fireFileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
    }

    public void handleWifiDirectConnectionChanged(String ssid){
        fireWiFiConnectionChanged(ssid);
    }



    protected void fireFileStatusCheckInformationAvailable(List<String> fileIds) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileStatusCheckInformationAvailable(fileIds);
            }
        }
    }


    protected void fireFileAcquisitionInformationAvailable(String entryId,long downloadId,int downloadSource) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.fileAcquisitionInformationAvailable(entryId,downloadId,downloadSource);
            }
        }
    }

    protected void fireNetworkNodeDiscovered(NetworkNode node) {
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkNodeDiscovered(node);
            }
        }
    }

    protected void fireNetworkNodeUpdated(NetworkNode node){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.networkNodeUpdated(node);
            }
        }
    }

    protected void fireEntryStatusCheckCompleted(NetworkTask task){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.entryStatusCheckCompleted(task);
            }
        }
    }

    protected void fireWiFiConnectionChanged(String ssid){
        synchronized (networkManagerListeners) {
            for(NetworkManagerListener listener : networkManagerListeners){
                listener.wifiConnectionChanged(ssid);
            }
        }
    }
    public abstract int addNotification(int notificationType,String title,String message);

    public abstract void updateNotification(int notificationId,int progress,String title,String message);

    public abstract void removeNotification(int notificationId);

    @Override
    public void handleTaskCompleted(NetworkTask task) {
        if(task == currentTasks[task.getTaskType()]) {
            currentTasks[task.getTaskType()] = null;
            checkTaskQueue(task.getTaskType());
        }

        fireEntryStatusCheckCompleted(task);
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

    public abstract String getDeviceIPAddress();


    public void handleWifiDirectGroupCreated(WiFiDirectGroup wiFiDirectGroup){
        fireWifiDirectGroupCreated(wiFiDirectGroup,null);
    }

    public void handleWifiDirectGroupRemoved(boolean isGroupRemoved){
        fireWifiDirectGroupRemoved(isGroupRemoved,null);
    }



    public abstract void connectWifi(String SSID,String passPhrase);

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
     * Remove the given WifiDirectGroupListener
     *
     * @param listener Listener to remove
     */
    public void removeWifiDirectGroupListener(WiFiDirectGroupListener listener) {
        wifiDirectGroupListeners.remove(listener);
    }

    protected void fireWifiDirectGroupCreated(WiFiDirectGroup group, Exception error) {
        synchronized (wifiDirectGroupListeners) {
            for(int i = 0; i < wifiDirectGroupListeners.size(); i++) {
                wifiDirectGroupListeners.get(i).groupCreated(group, error);
            }
        }
    }

    protected void fireWifiDirectGroupRemoved(boolean successful, Exception error) {
        synchronized (wifiDirectGroupListeners) {
            for(int i = 0; i < wifiDirectGroupListeners.size(); i++) {
                wifiDirectGroupListeners.get(i).groupRemoved(successful, error);
            }
        }
    }


    public void addAcquisitionTaskListener(AcquisitionListener listener){
        acquisitionListeners.add(listener);
    }


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
    protected void fireAcquisitionStatusChanged(String entryId, AcquisitionTask task){
        synchronized (acquisitionListeners) {
            for(AcquisitionListener listener : acquisitionListeners){
                listener.acquisitionStatusChanged(entryId, task.getStatusByEntryId(entryId));
            }
        }
    }

    /**
     * Find the acquisition task for the given entry id
     *
     * @param entryId Entry ID to find
     * @return The task carrying out acquisition of this entry, or null if it's not being acquired
     */
    public AcquisitionTask getAcquisitionTaskByEntryId(String entryId) {
        return getEntryAcquisitionTaskMap().get(entryId);
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
     * Reconnect the previous connected wifi after Wifi-Direct
     * acquisitionTask completion.
     */
    public abstract void reconnectPreviousNetwork();

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

    public void unmountZipFromHttp(String mountName) {
        httpd.unmountZip(mountName);
    }

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


}
