package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.util.LiveDataWorkQueue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import fi.iki.elonen.router.RouterNanoHTTPD;

import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.ARG_DOWNLOAD_SET_UID;

/**
 * This is an abstract class which is used to implement platform specific NetworkManager
 *
 * @author kileha3
 *
 */

public abstract class NetworkManagerBle implements LocalAvailabilityMonitor,
        LiveDataWorkQueue.OnQueueEmptyListener, EmbeddedHTTPD.ClientActivityListener {

    /**
     * Flag to indicate wifi direct group is inactive and it is not under creation
     */
    public static final int WIFI_DIRECT_GROUP_INACTIVE_STATUS = 0;

    /**
     * Flag to indicate Wifi direct group is being created now
     */
    public static final int WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS = 1;

    /**
     * Flag to indicate Wifi direct group is active
     */
    public static final int WIFI_DIRECT_GROUP_ACTIVE_STATUS = 2;

    /**
     * Flag to indicate entry status request
     */
    public static final byte ENTRY_STATUS_REQUEST = (byte) 111;

    /**
     * Flag to indicate entry status response
     */
    public static final byte ENTRY_STATUS_RESPONSE = (byte) 112;

    /**
     * Flag to indicate WiFi direct group request (for content download)
     */
    public static final byte WIFI_GROUP_REQUEST = (byte) 113;

    /**
     * Flag to indicate WiFi direct group creation response
     */
    public static final byte WIFI_GROUP_CREATION_RESPONSE = (byte) 114;


    /**
     * Commonly used MTU for android devices
     */
    public static final int DEFAULT_MTU_SIZE = 20;

    /**
     * Maximum MTU for the packet transfer
     */
    public static final int MAXIMUM_MTU_SIZE = 512;

    /**
     * Wifi direct change current status
     */
    protected int wifiDirectGroupChangeStatus = 0;

    /**
     * Bluetooth Low Energy service UUID for our app
     */
    public static final UUID USTADMOBILE_BLE_SERVICE_UUID =
            UUID.fromString("7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93e");

    public static final String WIFI_DIRECT_GROUP_SSID_PREFIX="DIRECT-";

    private final Object knownNodesLock = new Object();

    private Object mContext;

    private boolean isStopMonitoring = false;

    private ExecutorService entryStatusTaskExecutorService = Executors.newFixedThreadPool(5);

    private Map<Object, List<Long>> availabilityMonitoringRequests = new HashMap<>();

    private static final int MAX_THREAD_COUNT = 1;

    protected URLConnectionOpener localConnectionOpener;

    /**
     * Holds all created entry status tasks
     */
    private Vector<BleEntryStatusTask> entryStatusTasks = new Vector<>();

    /**
     * Lis of all objects that will be listening for the Wifi direct group change
     */
    private Vector<WiFiDirectGroupListenerBle> wiFiDirectGroupListeners = new Vector<>();

    private LiveDataWorkQueue<DownloadJobItemWithDownloadSetItem> downloadJobItemWorkQueue;

    private Map<Long, List<EntryStatusResponse>> entryStatusResponses = new Hashtable<>();

    private Set<Long> locallyAvailableContainerUids = new HashSet<>();

    protected AtomicReference<ConnectivityStatus> connectivityStatusRef = new AtomicReference<>();

    protected List<Object> wifiLockHolders = new Vector<>();

    public static final int DEFAULT_WIFI_CONNECTION_TIMEOUT = 30 * 1000;

    /**
     * Constructor to be used when creating new instance
     * @param context Platform specific application context
     */
    public NetworkManagerBle(Object context) {
        this.mContext = context;
    }

    private UmAppDatabase umAppDatabase;

    private Vector<LocalAvailabilityListener> localAvailabilityListeners = new Vector<>();

    /**
     * Constructor to be used for testing purpose (mocks)
     */
    public NetworkManagerBle(){ }

    /**
     * Set platform specific context
     * @param context Platform's context to be set
     */
    public void setContext(Object context){
        this.mContext = context;
    }

    private LiveDataWorkQueue.WorkQueueItemAdapter<DownloadJobItemWithDownloadSetItem>
        mJobItemAdapter = new LiveDataWorkQueue.WorkQueueItemAdapter<DownloadJobItemWithDownloadSetItem>() {
        @Override
        public Runnable makeRunnable(DownloadJobItemWithDownloadSetItem item) {
            return new DownloadJobItemRunner(mContext, item, NetworkManagerBle.this,
                    umAppDatabase, UmAccountManager.getRepositoryForActiveAccount(mContext),
                    UmAccountManager.getActiveEndpoint(mContext),
                    connectivityStatusRef.get());
        }

        @Override
        public long getUid(DownloadJobItemWithDownloadSetItem item) {
            return ((long)(Long.valueOf(item.getDjiUid()).hashCode()) << 32) | item.getNumAttempts();
        }
    };

    /**
     * Start web server, advertising and discovery
     */
    public void onCreate() {
        umAppDatabase = UmAppDatabase.getInstance(mContext);
        downloadJobItemWorkQueue = new LiveDataWorkQueue<>(MAX_THREAD_COUNT);
        downloadJobItemWorkQueue.setAdapter(mJobItemAdapter);
        downloadJobItemWorkQueue.start(umAppDatabase.getDownloadJobItemDao().findNextDownloadJobItems());
    }

    @Override
    public void onQueueEmpty() {
        if(connectivityStatusRef.get() != null
                && connectivityStatusRef.get().getConnectivityState() == ConnectivityStatus.STATE_CONNECTED_LOCAL) {
            new Thread(this::restoreWifi).start();
        }
    }

    @Override
    public void OnClientListChanged(Map<String, Long> clientIpToLastActiveMap) {
        if(clientIpToLastActiveMap.isEmpty() && getWifiDirectGroup() != null){
            UstadMobileSystemImpl.l(UMLog.INFO, 699,
                    "No more clients, removing wifi direct group");
            removeWifiDirectGroup();
        }
    }

    /**
     * Check if WiFi is enabled / disabled on the device
     * @return boolean true, if enabled otherwise false.
     */
    public abstract boolean isWiFiEnabled();


    /**
     * Check if the device is Bluetooth Low Energy capable
     * @return True is capable otherwise false
     */
    public abstract boolean isBleCapable();

    /**
     * Check if bluetooth is enabled on the device
     * @return True if enabled otherwise false
     */
    public abstract boolean isBluetoothEnabled();

    /**
     * Check if the device can create BLE service and advertise it to the peer devices
     * @return true if can advertise its service else false
     */
    public abstract boolean canDeviceAdvertise();

    /**
     * Start advertising BLE service to the peer devices
     * <b>Use case</b>
     * When this method called, it will create BLE service and start advertising it.
     */
    public abstract void startAdvertising();

    /**
     * Stop advertising the service which was created and advertised by
     * {@link NetworkManagerBle#startAdvertising()}
     */
    public abstract void stopAdvertising();

    /**
     * Start scanning for the peer devices whose services are being advertised
     */
    public abstract void startScanning();

    /**
     * Stop scanning task which was started by {@link NetworkManagerBle#startScanning()}
     */
    public abstract void stopScanning();

    /**
     * This should be called by the platform implementation when BLE discovers a nearby device
     * @param node The nearby device discovered
     */
    protected void handleNodeDiscovered(NetworkNode node) {
        synchronized (knownNodesLock){
            long updateTime = Calendar.getInstance().getTimeInMillis();

            NetworkNodeDao networkNodeDao = UmAppDatabase.getInstance(mContext).getNetworkNodeDao();
            networkNodeDao.updateLastSeen(node.getBluetoothMacAddress(),updateTime,
                    new UmCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            if(result == 0){
                                List<Long> entryUidsToMonitor =
                                        new ArrayList<>(getAllUidsToBeMonitored());
                                if(!isStopMonitoring){
                                    if(entryUidsToMonitor.size() > 0){
                                        BleEntryStatusTask entryStatusTask =
                                                makeEntryStatusTask(mContext,entryUidsToMonitor,node);
                                        entryStatusTasks.add(entryStatusTask);
                                        entryStatusTaskExecutorService.execute(entryStatusTask);
                                    }
                                    node.setNetworkNodeLastUpdated(updateTime);
                                    networkNodeDao.insert(node);
                                    UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                                "New node added to the database , address = "
                                        +node.getBluetoothMacAddress()
                                        + (entryUidsToMonitor.size() > 0 ? " monitor task created"
                                        : " no entriries to be monitored"));

                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                                    "NetworkNode updated failed",new Exception(exception));
                        }
                    });
        }
    }

    /**
     * Open bluetooth setting section from setting panel
     */
    public abstract void openBluetoothSettings();

    /**
     * Enable or disable WiFi on the device
     *
     * @param enabled Enable when true otherwise disable
     * @return true if the operation is successful, false otherwise
     */
    public abstract boolean setWifiEnabled(boolean enabled);

    /**
     * Create a new WiFi direct group on this device. A WiFi direct group
     * will create a new SSID and passphrase other devices can use to connect in "legacy" mode.
     *
     * The process is asynchronous and the {@link WiFiDirectGroupListenerBle} should be used to
     * listen for group creation.
     *
     * If a WiFi direct group is already under creation this method has no effect.
     */
    public abstract void createWifiDirectGroup();

    /**
     * Get current active WiFi Direct group (if any)
     *
     * @return The active WiFi direct group (if any) - otherwise null
     */
    public abstract WiFiDirectGroupBle getWifiDirectGroup();

    /**
     * Remove a WiFi group from the device.The process is asynchronous and the
     * {@link WiFiDirectGroupListenerBle} should be used to listen for
     * group removal.
     */
    public abstract void removeWifiDirectGroup();

    /**
     * Get Wifi direct group change status
     * @return Current status
     */
    int getWifiDirectGroupChangeStatus() {
        return wifiDirectGroupChangeStatus;
    }

    /**
     * Set Wifi direct group change status
     * @param wifiDirectGroupChangeStatus Status to be changed to
     */
    void setWifiDirectGroupChangeStatus(int wifiDirectGroupChangeStatus) {
        this.wifiDirectGroupChangeStatus = wifiDirectGroupChangeStatus;
    }

    /**
     * Add all group change listeners to the list
     * @param groupListenerBle Listener interface
     *
     * @see WiFiDirectGroupListenerBle
     */
    void handleWiFiDirectGroupChangeRequest(WiFiDirectGroupListenerBle groupListenerBle){
        if(!wiFiDirectGroupListeners.contains(groupListenerBle)){
            wiFiDirectGroupListeners.add(groupListenerBle);
        }
    }

    /**
     * Notify all listening object that Wifi direct group has been changed
     * @param isCreated True if change was CREATION otherwise REMOVAL
     * @param group Group information
     *
     * @see WiFiDirectGroupBle
     */
    protected void fireWiFiDirectGroupChanged(boolean isCreated, WiFiDirectGroupBle group){
        for(WiFiDirectGroupListenerBle groupListenerBle : wiFiDirectGroupListeners){
            if(isCreated){
                groupListenerBle.groupCreated(group,null);
            }else{
                groupListenerBle.groupRemoved(true,null);
            }
        }
    }


    /**
     * Start monitoring availability of specific entries from peer devices
     * @param monitor Object to monitor e.g Presenter
     * @param entryUidsToMonitor List of entries to be monitored
     */
    @Override
    public void startMonitoringAvailability(Object monitor, List<Long> entryUidsToMonitor) {
        availabilityMonitoringRequests.put(monitor, entryUidsToMonitor);
        UstadMobileSystemImpl.l(UMLog.DEBUG,694, "Registered a monitor with "
                + entryUidsToMonitor.size() + " entry(s) to be monitored");

        NetworkNodeDao networkNodeDao = UmAppDatabase.getInstance(mContext).getNetworkNodeDao();
        EntryStatusResponseDao responseDao =
                UmAppDatabase.getInstance(mContext).getEntryStatusResponseDao();

        long lastUpdateTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);

        List<Long> uniqueEntryUidsToMonitor = new ArrayList<>(getAllUidsToBeMonitored());
        List<Long> knownNetworkNodes =
                getAllKnownNetworkNodeIds(networkNodeDao.findAllActiveNodes(lastUpdateTime,1));

        UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                "Found total of   " + uniqueEntryUidsToMonitor +
                        " to check from entry status availability");

        List<EntryStatusResponseDao.EntryWithoutRecentResponse> entryWithoutRecentResponses =
                responseDao.findEntriesWithoutRecentResponse(uniqueEntryUidsToMonitor,knownNetworkNodes,
                        System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));

        //Group entryUUid by node where their status will be checked from
        LinkedHashMap<Integer,List<Long>> nodeToCheckEntryList = new LinkedHashMap<>();
        for(EntryStatusResponseDao.EntryWithoutRecentResponse entryResponse: entryWithoutRecentResponses){
            int nodeIdToCheckFrom = entryResponse.getNodeId();
            if(!nodeToCheckEntryList.containsKey(nodeIdToCheckFrom))
                nodeToCheckEntryList.put(nodeIdToCheckFrom, new ArrayList<>());

            nodeToCheckEntryList.get(nodeIdToCheckFrom).add(entryResponse.getContainerUid());
        }

        UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                "Created total of  "+nodeToCheckEntryList.entrySet().size()
                        + "entry(s) to be checked from");

        //Make entryStatusTask as per node list and entryUuids found
        for(int nodeId : nodeToCheckEntryList.keySet()){
            NetworkNode networkNode = networkNodeDao.findNodeById(nodeId);
            BleEntryStatusTask entryStatusTask = makeEntryStatusTask(mContext,
                    nodeToCheckEntryList.get(nodeId),networkNode);
            entryStatusTasks.add(entryStatusTask);
            entryStatusTaskExecutorService.execute(entryStatusTask);
            UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                    "Status check started for "+nodeToCheckEntryList.get(nodeId).size()
                            + " entry(s) task from "+networkNode.getBluetoothMacAddress());
        }
    }

    /**
     * Stop monitoring the availability of entries from peer devices
     * @param monitor Monitor object which created a monitor (e.g Presenter)
     */
    @Override
    public void stopMonitoringAvailability(Object monitor) {
        availabilityMonitoringRequests.remove(monitor);
        isStopMonitoring = availabilityMonitoringRequests.size() == 0;
    }

    /**
     * Get all unique entry UUID's to be monitored
     * @return Set of all unique UUID's
     */
    private SortedSet<Long> getAllUidsToBeMonitored() {
        SortedSet<Long> uidsToBeMonitoredSet = new TreeSet<>();
        for(List<Long> uidList : availabilityMonitoringRequests.values()) {
            uidsToBeMonitoredSet.addAll(uidList);
        }
        return uidsToBeMonitoredSet;
    }

    /**
     * Get all peer network nodes that we know about
     * @param networkNodes Known NetworkNode
     * @return List of all known nodes
     */
    private List<Long> getAllKnownNetworkNodeIds(List<NetworkNode> networkNodes){
        List<Long> nodeIdList = new ArrayList<>();
        for(NetworkNode networkNode: networkNodes){
            nodeIdList.add(networkNode.getNodeId());
        }
        return nodeIdList;
    }

    /**
     * Connecting a client to a group network for content acquisition
     * @param ssid Group network SSID
     * @param passphrase Group network passphrase
     */
    public abstract void connectToWiFi(String ssid, String passphrase, int timeout);

    public void connectToWiFi(String ssid, String passphrase) {
        connectToWiFi(ssid, passphrase, DEFAULT_WIFI_CONNECTION_TIMEOUT);
    }

    /**
     * Restore the 'normal' WiFi connection
     */
    public abstract void restoreWifi();


    /**
     * Create entry status task for a specific peer device,
     * it will request status of the provided entries from the provided peer device
     * @param context Platform specific mContext
     * @param entryUidsToCheck List of entries to be checked from the peer device
     * @param peerToCheck Peer device to request from
     * @return Created BleEntryStatusTask
     *
     * @see BleEntryStatusTask
     */
    public abstract BleEntryStatusTask makeEntryStatusTask(Object context,List<Long> entryUidsToCheck, NetworkNode peerToCheck);

    /**
     * Create entry status task for a specific peer device,
     * it will request status of the provided entries from the provided peer device
     * @param context Platform specific mContext
     * @param message Message to be sent to the peer device
     * @param peerToSendMessageTo Peer device to send message to.
     * @param responseListener Message response listener object
     * @return Created BleEntryStatusTask
     *
     * @see BleEntryStatusTask
     */
    public abstract BleEntryStatusTask makeEntryStatusTask(Object context,BleMessage message,
                                                           NetworkNode peerToSendMessageTo,
                                                           BleMessageResponseListener responseListener);

    public abstract DeleteJobTaskRunner makeDeleteJobTask(Object object, Hashtable args);

    /**
     * Send message to a specific device
     * @param context Platform specific context
     * @param message Message to be send
     * @param peerToSendMessageTo Peer device to receive the message
     * @param responseListener Message response listener object
     */
    public void sendMessage(Object context, BleMessage message, NetworkNode peerToSendMessageTo,
                            BleMessageResponseListener responseListener){
        makeEntryStatusTask(context,message,peerToSendMessageTo, responseListener).run();
    }

    /**
     * @return Active RouterNanoHTTPD
     */
    public abstract RouterNanoHTTPD getHttpd();



    /**
     * Cancel all download set and set items
     * @param args Arguments to be passed to the task runner.
     */
    public void cancelAndDeleteDownloadSet(Hashtable args) {

        long downloadSetUid = Long.parseLong(String.valueOf(args.get(ARG_DOWNLOAD_SET_UID)));
        List<DownloadJob> downloadJobs = umAppDatabase.getDownloadJobDao().
                findBySetUid(downloadSetUid);

        for(DownloadJob downloadJob : downloadJobs){
            umAppDatabase.getDownloadJobDao().updateJobAndItems(downloadJob.getDjUid(),
                    JobStatus.CANCELED, -1, JobStatus.CANCELED);
        }

        makeDeleteJobTask(mContext,args).run();
    }

    /**
     * Send p2p state changes to either stop or start p2p service advertising & broadcasting
     */
    public abstract void sendP2PStateChangeBroadcast();


    public void addLocalAvailabilityListener(LocalAvailabilityListener listener) {
        localAvailabilityListeners.add(listener);
    }

    public void removeLocalAvailabilityListener(LocalAvailabilityListener listener) {
        localAvailabilityListeners.remove(listener);
    }

    public void fireLocalAvailabilityChanged() {
        List<LocalAvailabilityListener> listenerList = new ArrayList<>(localAvailabilityListeners);
        for(LocalAvailabilityListener listener : listenerList) {
            listener.onLocalAvailabilityChanged(locallyAvailableContainerUids);
        }
    }


    public void handleLocalAvailabilityResponsesReceived(List<EntryStatusResponse> responses) {
        if(responses.isEmpty())
            return;

        long nodeId = responses.get(0).getErNodeId();
        if(!entryStatusResponses.containsKey(nodeId))
            entryStatusResponses.put(nodeId, new Vector<>());

        entryStatusResponses.get(nodeId).addAll(responses);
        locallyAvailableContainerUids.clear();

        for(List<EntryStatusResponse> responseList : entryStatusResponses.values()) {
            for(EntryStatusResponse response : responseList) {
                if(response.isAvailable())
                    locallyAvailableContainerUids.add(response.getErContainerUid());
            }
        }

        fireLocalAvailabilityChanged();
    }

    /**
     * @return Active URLConnectionOpener
     */
    public URLConnectionOpener getLocalConnectionOpener() {
        return localConnectionOpener;
    }

    public void lockWifi(Object lockHolder) {
        wifiLockHolders.add(lockHolder);
    }

    public void releaseWifiLock(Object lockHolder) {
        wifiLockHolders.remove(lockHolder);
    }


    /**
     * Clean up the network manager for shutdown
     */
    public void onDestroy(){
        //downloadJobItemWorkQueue.shutdown();
        wiFiDirectGroupListeners.clear();
        entryStatusTaskExecutorService.shutdown();
    }
}
