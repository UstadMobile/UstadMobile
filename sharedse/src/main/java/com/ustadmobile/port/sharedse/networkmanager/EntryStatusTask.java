package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.NetworkNode;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BluetoothServer.CMD_SEPARATOR;

/**
 * <h1>EntryStatusTask</h1>
 *
 * This is a class which is responsible to handle all entry status check task.
 *
 * @see NetworkTask
 * @see com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler
 *
 * @author kileha3
 */

public class EntryStatusTask extends NetworkTask implements BluetoothConnectionHandler{

    private List<String> entryIdList;

    private List<NetworkNode> networkNodeList;

    private NetworkNodeListProvider nodeListProvider;

    private int currentNode;

    protected NetworkManager networkManager;

    private boolean stopped = false;

    public static final String ENTRY_RESPONSE_KEY_AVAILABLE = "a";

    public static final String ENTRY_RESPONSE_ENTRIES_KEY = "e";

    public static final int HTTP_CONNECT_TIMEOUT = 6000;

    interface NetworkNodeListProvider {

        List<NetworkNode> getNetworkNodes();

    }


    public EntryStatusTask(List<String> entryIdList, NetworkNodeListProvider nodeListProvider, NetworkManager networkManager){
        super(networkManager);
        this.networkManager = networkManager;
        this.entryIdList = entryIdList;
        this.nodeListProvider = nodeListProvider;
    }

    @Override
    public void start() {
        currentNode = 0;
        networkNodeList = nodeListProvider.getNetworkNodes();
        setStatus(STATUS_RUNNING);
        new Thread(() -> connectNextNode(0)).start();
    }

    @Override
    public synchronized void stop(int statusAfterStop) {
        stopped = true;
        setStatus(statusAfterStop);
        networkManager.networkTaskStatusChanged(this);
    }

    @Override
    public synchronized boolean isStopped() {
        return stopped;
    }

    private String mkLogPrefix() {
        return "EntryStatusTask #" + getTaskId();
    }

    private void connectNextNode(int index) {
        if(isStopped()) {
            return;
        }else if(index < networkNodeList.size()) {
            UstadMobileSystemImpl.l(UMLog.VERBOSE, 400, mkLogPrefix() + " connect node #" + index);
            currentNode = index;
            NetworkNode node = networkNodeList.get(currentNode);

            //TODO : check if node is on same subnet, for now just insist it was discovered using NSD
            if(networkManager.isWiFiEnabled() && isUseHttp()
                    && networkManager.getDeviceIPAddress() != null
                    && node.getNsdServiceName() != null
                    && node.getIpAddress() != null) {
                UstadMobileSystemImpl.l(UMLog.VERBOSE, 400, mkLogPrefix() + " connect node #"
                        + index + " - http to " + node.getIpAddress() + ":" + node.getPort()
                        + " (" + node.getNsdServiceName() + ")");
                getEntryStatusHttp(node);
                connectNextNode(index + 1);
            }else if(networkManager.isBluetoothEnabled() && isUseBluetooth()
                        && node.getBluetoothMacAddress() != null) {
                String bluetoothAddr = networkNodeList.get(currentNode).getBluetoothMacAddress();
                UstadMobileSystemImpl.l(UMLog.VERBOSE, 400, mkLogPrefix() + " connect node #" + index
                        + " to bluetooth addr " + bluetoothAddr);
                networkManager.connectBluetooth(bluetoothAddr, this);
            }else {
                //skip it - not possible to query this node with the current setup
                UstadMobileSystemImpl.l(UMLog.VERBOSE, 400, mkLogPrefix() + " connect node #" + index + " - no suitable method to connect");
                connectNextNode(index+1);
            }
        }else {
            setStatus(STATUS_COMPLETE);
            networkManager.networkTaskStatusChanged(this);
        }
    }

    protected void getEntryStatusHttp(NetworkNode node) {
        HttpURLConnection connection = null;
        OutputStream httpOut = null;
        Reader httpReader = null;
        try {
            URL apiEndpointUrl = new URL("http://" + node.getIpAddress() + ":"
                    + node.getPort() + "/catalog/entry_status");
            JSONObject entryIds = new JSONObject();
            JSONArray entryArr = new JSONArray(entryIdList);
            entryIds.put(ENTRY_RESPONSE_ENTRIES_KEY, entryArr);
            byte[] postPayload = entryIds.toString().getBytes("UTF-8");

            connection = (HttpURLConnection)apiEndpointUrl.openConnection();
            connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            httpOut = connection.getOutputStream();
            httpOut.write(postPayload);
            httpOut.flush();
            httpOut.close();

            connection.connect();
            int responseCode = connection.getResponseCode();

            if(responseCode >= 200 &&  responseCode < 300) {
                httpReader = new InputStreamReader(connection.getInputStream());
                Type gsonType = new TypeToken<List<ContainerFileEntry>>(){}.getType();
                List<ContainerFileEntry> containerFileEntries = new Gson().fromJson(httpReader,
                        gsonType);

                UstadMobileSystemImpl.l(UMLog.INFO, 376, mkLogPrefix()
                        + " - successfully processed http response");

                networkManager.handleEntriesStatusUpdate(node, entryIdList, containerFileEntries);

            }
        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeOutputStream(httpOut);
            UMIOUtils.closeQuietly(httpReader);
        }

        UstadMobileSystemImpl.l(UMLog.INFO, 382, mkLogPrefix() + " - done with node");
    }

    /**
     * @exception IOException
     * @param inputStream InputStream to read data from.
     * @param outputStream OutputStream to write data to.
     */
    @Override
    public void onBluetoothConnected(InputStream inputStream, OutputStream outputStream) {
        UstadMobileSystemImpl.l(UMLog.VERBOSE, 438, mkLogPrefix() + " bluetooth connected");
        String queryStr = BluetoothServer.CMD_ENTRY_STATUS_QUERY + ' ';
        List<Boolean> entryIdStatusList=new ArrayList<>();
        for(int i = 0; i < entryIdList.size(); i++){
            try { queryStr += URLEncoder.encode(entryIdList.get(i), "UTF-8"); }
            catch(UnsupportedEncodingException ignored) {}//what device doesn't have UTF-8?

            if(i < entryIdList.size() - 1)
                queryStr += CMD_SEPARATOR;
        }

        queryStr += '\n';
        String response=null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream.write(queryStr.getBytes());
            response = reader.readLine();
            Type gsonType = new TypeToken<List<ContainerFileEntry>>(){}.getType();
            List<ContainerFileEntry> availableEntries = new Gson().fromJson(response, gsonType);
            networkManager.handleEntriesStatusUpdate(networkNodeList.get(currentNode), entryIdList,
                    availableEntries);
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 78, mkLogPrefix() + " onBluetoothConnected IO Exception", e);
        }finally {
            if(reader != null){
                try {reader.close();}
                catch(IOException e){}
            }
            UMIOUtils.closeInputStream(inputStream);
            UMIOUtils.closeOutputStream(outputStream);
        }

        connectNextNode(currentNode+1);
    }

    @Override
    public void onBluetoothConnectionFailed(Exception exception) {
        UstadMobileSystemImpl.l(UMLog.WARN, 212, mkLogPrefix() + " bluetooth connection failed",
                exception);
        connectNextNode(currentNode + 1);
    }

    @Override
    public int getQueueId() {
        return NetworkManagerCore.QUEUE_ENTRY_STATUS;
    }

    @Override
    //TODO: fix this return type
    public int getTaskType() {
        return 0;
    }

}
