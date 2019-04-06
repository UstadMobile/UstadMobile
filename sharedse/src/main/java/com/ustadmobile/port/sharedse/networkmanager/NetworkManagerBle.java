package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.DownloadJobItemManager;
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.util.LiveDataWorkQueue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.ARG_DOWNLOAD_SET_UID;

/**
 * This is an abstract class which is used to implement platform specific NetworkManager
 *
 * @author kileha3
 *
 */

public abstract class NetworkManagerBle implements LocalAvailabilityMonitor,
        LiveDataWorkQueue.OnQueueEmptyListener {

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
     * Bluetooth Low Energy service UUID for our app
     */
    public static final UUID USTADMOBILE_BLE_SERVICE_UUID =
            UUID.fromString("7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93a");

    public static final String WIFI_DIRECT_GROUP_SSID_PREFIX="DIRECT-";

    private final Object knownNodesLock = new Object();

    private Object mContext;

    private boolean isStopMonitoring = false;

    private ExecutorService entryStatusTaskExecutorService = Executors.newFixedThreadPool(5);

    private Map<Object, List<Long>> availabilityMonitoringRequests = new HashMap<>();

    protected HashMap<String, AtomicInteger> knownBadNodeTrackList = new HashMap<>();

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

    private Map<String, Long> knownPeerNodes = new HashMap<>();

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

    private UmAppDatabase umAppDatabase;

    private Vector<LocalAvailabilityListener> localAvailabilityListeners = new Vector<>();

    /**
     * Constructor to be used when creating new instance
     * @param context Platform specific application context
     */
    public NetworkManagerBle(Object context) {
        this.mContext = context;
    }

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

    private Runnable nodeLastSeenTrackerTask = new Runnable() {
        @Override
        public void run() {
            if(knownPeerNodes.size() > 0){
                Map<String , Long> nodeMap = new HashMap<>(knownPeerNodes);
                umAppDatabase.getNetworkNodeDao().updateNodeLastSeen(nodeMap);
                UstadMobileSystemImpl.l(UMLog.DEBUG,694, "Updating "
                        + knownPeerNodes.size() + " nodes from the Db");
            }
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
        scheduledExecutorService.scheduleAtFixedRate(nodeLastSeenTrackerTask,0,10,TimeUnit.SECONDS);
    }

    @Override
    public void onQueueEmpty() {
        if(connectivityStatusRef.get() != null
                && connectivityStatusRef.get().getConnectivityState() == ConnectivityStatus.STATE_CONNECTED_LOCAL) {
            new Thread(this::restoreWifi).start();
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
     * This should be called by the platform implementation when BLE discovers a nearby device
     * @param node The nearby device discovered
     */
    protected void handleNodeDiscovered(NetworkNode node) {
        synchronized (knownNodesLock){

            NetworkNodeDao networkNodeDao = UmAppDatabase.getInstance(mContext).getNetworkNodeDao();

            if(!knownPeerNodes.containsKey(node.getBluetoothMacAddress())){

                node.setLastUpdateTimeStamp(System.currentTimeMillis());

                networkNodeDao.updateLastSeen(node.getBluetoothMacAddress(),
                        node.getLastUpdateTimeStamp(), new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        knownPeerNodes.put(node.getBluetoothMacAddress(),
                                node.getLastUpdateTimeStamp());
                        if(result == 0){
                            networkNodeDao.insertAsync(node, null);
                            UstadMobileSystemImpl.l(UMLog.DEBUG,694, "New node with address "
                                    + node.getBluetoothMacAddress() + " found, added to the Db");

                            List<Long> entryUidsToMonitor = new ArrayList<>(getAllUidsToBeMonitored());

                            if(!isStopMonitoring){
                                if(entryUidsToMonitor.size() > 0){
                                    BleEntryStatusTask entryStatusTask =
                                            makeEntryStatusTask(mContext,entryUidsToMonitor,node);
                                    entryStatusTasks.add(entryStatusTask);
                                    entryStatusTaskExecutorService.execute(entryStatusTask);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });

            }else{
                knownPeerNodes.put(node.getBluetoothMacAddress(),System.currentTimeMillis());
            }
        }
    }

    public abstract WiFiDirectGroupBle awaitWifiDirectGroupReady(long timeout, TimeUnit timeoutUnit);

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
     * Start monitoring availability of specific entries from peer devices
     * @param monitor Object to monitor e.g Presenter
     * @param entryUidsToMonitor List of entries to be monitored
     */
    @Override
    public void startMonitoringAvailability(Object monitor, List<Long> entryUidsToMonitor) {
        try{
            //isStopMonitoring = false;
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
                    "Found total of   " + uniqueEntryUidsToMonitor.size() +
                            " uids to check their availability status");

            List<EntryStatusResponseDao.EntryWithoutRecentResponse> entryWithoutRecentResponses =
                    responseDao.findEntriesWithoutRecentResponse(
                            uniqueEntryUidsToMonitor, knownNetworkNodes,
                            System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));

            //Group entryUUid by node where their status will be checked from
            LinkedHashMap<Integer,List<Long>> nodeToCheckEntryList = new LinkedHashMap<>();

            for(EntryStatusResponseDao.EntryWithoutRecentResponse entryResponse:
                    entryWithoutRecentResponses){

                int nodeIdToCheckFrom = entryResponse.getNodeId();
                if(!nodeToCheckEntryList.containsKey(nodeIdToCheckFrom))
                    nodeToCheckEntryList.put(nodeIdToCheckFrom, new ArrayList<>());

                nodeToCheckEntryList.get(nodeIdToCheckFrom).add(entryResponse.getContainerUid());
            }

            UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                    "Created total of  " + nodeToCheckEntryList.entrySet().size()
                            + " entry(s) to be checked from");

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
        }catch (RejectedExecutionException e){
            e.printStackTrace();
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

    public abstract DeleteJobTaskRunner makeDeleteJobTask(Object object, Map<String , String>  args);

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
    public abstract EmbeddedHTTPD getHttpd();



    /**
     * Cancel all download set and set items
     * @param args Arguments to be passed to the task runner.
     */
    public void cancelAndDeleteDownloadSet(Map<String , String>  args) {

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
     * Add listener to the list of local availability listeners
     * @param listener listener object to be added.
     */
    public void addLocalAvailabilityListener(LocalAvailabilityListener listener) {
        if(!localAvailabilityListeners.contains(listener)){
            localAvailabilityListeners.add(listener);
        }
    }

    /**
     * Remove a listener from a list of all available listeners
     * @param listener listener to be removed
     */
    public void removeLocalAvailabilityListener(LocalAvailabilityListener listener) {
        localAvailabilityListeners.remove(listener);
    }

    /**
     * Trigger availability status change event to all listening parts
     */
    public void fireLocalAvailabilityChanged() {
        List<LocalAvailabilityListener> listenerList = new ArrayList<>(localAvailabilityListeners);
        for(LocalAvailabilityListener listener : listenerList) {
            listener.onLocalAvailabilityChanged(locallyAvailableContainerUids);
        }
    }

    /**
     * All all availability statuses received from the peer node
     * @param responses response received
     */
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

    //testing purpose
    public void clearHistories(){
        locallyAvailableContainerUids.clear();
        knownPeerNodes.clear();
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
     * Handle node connection history, delete node which failed to connect for over 5 attempts
     * @param bluetoothAddress node bluetooth address
     * @param success connection status , True if the connection was made successfully,
     *               otherwise false
     */
    public void handleNodeConnectionHistory(String bluetoothAddress, boolean success){

        AtomicInteger record = knownBadNodeTrackList.get(bluetoothAddress);

        if(record == null || success){
            record = new AtomicInteger(0);
            knownBadNodeTrackList.put(bluetoothAddress,record);
            UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                    "Connection succeeded bad node counter was set to " + record.get()
                            + " for "+bluetoothAddress);
        }

        if(!success){
            record.set(record.incrementAndGet());
            knownBadNodeTrackList.put(bluetoothAddress,record);
            UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                    "Connection failed and bad node counter set to " + record.get()
                            + " for "+bluetoothAddress);
        }

        if(knownBadNodeTrackList.get(bluetoothAddress).get() > 5){
            UstadMobileSystemImpl.l(UMLog.DEBUG,694,
                    "Bad node counter exceeded threshold (5), removing node with address "
                            +bluetoothAddress + " from the list");
            knownBadNodeTrackList.remove(bluetoothAddress);
            knownPeerNodes.remove(bluetoothAddress);
            umAppDatabase.getNetworkNodeDao().deleteByBluetoothAddress(bluetoothAddress);

            UstadMobileSystemImpl.l(UMLog.DEBUG,694, "Node with address "
                            +bluetoothAddress + " removed from the list");
        }
    }

    /**
     * Get bad node by bluetooth address
     * @param bluetoothAddress node bluetooth address
     * @return bad node
     */
    public AtomicInteger getBadNodeTracker(String bluetoothAddress){
        return knownBadNodeTrackList.get(bluetoothAddress);
    }

    public boolean isEntryLocallyAvailable(long containerUid){
        return locallyAvailableContainerUids.contains(containerUid);
    }

    public Set<Long> getLocallyAvailableContainerUids(){
        return locallyAvailableContainerUids;
    }


    /**
     * Clean up the network manager for shutdown
     */
    public void onDestroy(){
        //downloadJobItemWorkQueue.shutdown();
        wiFiDirectGroupListeners.clear();
        entryStatusTaskExecutorService.shutdown();
    }

    /**
     * Convert IP address to decimals
     * @param address IPV4 address
     * @return decimal representation of an IP address
     */
    private int convertIpAddressToInteger(String address){
        int result = 0;
        String[] ipAddressInArray = address.split("\\.");
        for (int i = 3; i >= 0; i--){
            int ip = Integer.parseInt(ipAddressInArray[3 - i]);
            result |= ip << (i * 8);
        }
        return result;
    }

    /**
     * Convert decimal representation of an ip address back to IPV4 format.
     * @param ip decimal representation
     * @return IPV4 address
     */
    private  String convertIpAddressToString(int ip){
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }


    /**
     * Convert group information to bytes so that they can be transmitted using {@link BleMessage}
     * @param group WiFiDirectGroupBle
     * @return constructed bytes  array from the group info.
     */

    public byte [] getWifiGroupInfoAsBytes(WiFiDirectGroupBle group){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte [] infoAsbytes = new byte[]{};
        DataOutputStream outputStream = new DataOutputStream(bos);
        try {
            outputStream.writeUTF(group.getSsid());
            outputStream.writeUTF(group.getPassphrase());
            outputStream.writeInt(convertIpAddressToInteger(group.getIpAddress()));
            outputStream.writeChar((char)(group.getPort() + 'a'));
            infoAsbytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(outputStream);
            UMIOUtils.closeQuietly(bos);
        }
        return  infoAsbytes;
    }


    /**
     * Construct WiFiDirectGroupBle from received message payload
     * @param payload received payload
     * @return constructed WiFiDirectGroupBle
     */
    public WiFiDirectGroupBle getWifiGroupInfoFromBytes(byte [] payload){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(payload);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        WiFiDirectGroupBle groupBle = null;
        try {
            groupBle = new WiFiDirectGroupBle(dataInputStream.readUTF(), dataInputStream.readUTF());
            groupBle.setIpAddress(convertIpAddressToString(dataInputStream.readInt()));
            groupBle.setPort(dataInputStream.readChar() - 'a');
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(inputStream);
            UMIOUtils.closeQuietly(dataInputStream);
        }

        UstadMobileSystemImpl.l(UMLog.INFO, 699,
                "Group information received with ssid = " + groupBle.getSsid());
        return groupBle;
    }

    public abstract boolean isVersionLollipopOrAbove();

    public abstract boolean isVersionKitKatOrBelow();

    /**
     * Inserts a DownloadJob into the database for a given
     *
     * @param newDownloadJob the new DownloadJob to be created (with properties set)
     *
     * @return
     */
    public DownloadJobItemManager createNewDownloadJobItemManager(DownloadJob newDownloadJob) {
        newDownloadJob.setDjUid(umAppDatabase.getDownloadJobDao().insert(newDownloadJob));
        return new DownloadJobItemManager(umAppDatabase, (int)newDownloadJob.getDjUid());
    }

    public DownloadJobItemManager createNewDownloadJobItemManager(long rootContentEntryUid) {
        return createNewDownloadJobItemManager(new DownloadJob(rootContentEntryUid,
                System.currentTimeMillis()));
    }


    public DownloadJobItemManager getDownloadJobItemManager(int downloadJobId) {
        return null;
    }

    public DownloadJobItemManager findDownloadJobItemManagerByContentEntryUid(long contentEntryUid) {
        return null;
    }

}
