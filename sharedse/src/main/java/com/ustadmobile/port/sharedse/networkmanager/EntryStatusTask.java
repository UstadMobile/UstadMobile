package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

    private int currentNode;

    protected NetworkManager networkManager;

    public EntryStatusTask(List<String> entryIdList, List<NetworkNode> networkNodeList, NetworkManager networkManager){
        super(networkManager);
        this.networkManager = networkManager;
        this.entryIdList = entryIdList;
        this.networkNodeList = networkNodeList;
    }
    @Override
    public void start() {
        currentNode = 0;
        setStatus(STATUS_RUNNING);
        new Thread(new Runnable() {
            public void run() {
                connectNextNode(0);
            }
        }).start();
    }

    @Override
    public synchronized void stop(int statusAfterStop) {
        super.stop(statusAfterStop);
        networkManager.networkTaskStatusChanged(this);
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
            if(networkManager.isBluetoothEnabled() && isUseBluetooth() && networkNodeList.get(currentNode).getDeviceBluetoothMacAddress() != null) {
                String bluetoothAddr = networkNodeList.get(currentNode).getDeviceBluetoothMacAddress();
                UstadMobileSystemImpl.l(UMLog.VERBOSE, 400, mkLogPrefix() + " connect node #" + index
                        + " to bluetooth addr " + bluetoothAddr);
                networkManager.connectBluetooth(bluetoothAddr, this);
            }else if(networkManager.isWiFiEnabled() && isUseHttp() && networkNodeList.get(currentNode).getDeviceIpAddress() != null) {
                //TODO: Handle status acquisition over http
                UstadMobileSystemImpl.l(UMLog.VERBOSE, 400, mkLogPrefix() + " connect node #" + index + " - network - skip");
                connectNextNode(index+1);
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
            if(response.startsWith(BluetoothServer.CMD_ENTRY_STATUS_FEEDBACK)) {
                response=response.substring((BluetoothServer.CMD_ENTRY_STATUS_FEEDBACK.length()+1),response.length());

                for(String status: response.split(CMD_SEPARATOR)){
                    boolean responseStatus= status.equals("1");
                    entryIdStatusList.add(responseStatus);
                }

                UstadMobileSystemImpl.l(UMLog.DEBUG, 648, mkLogPrefix() + " response: " + response);

                networkManager.handleEntriesStatusUpdate(networkNodeList.get(currentNode), entryIdList,entryIdStatusList);
            }else {
                System.out.print("Feedback "+response);
            }
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
        return NetworkManager.QUEUE_ENTRY_STATUS;
    }

    @Override
    //TODO: fix this return type
    public int getTaskType() {
        return 0;
    }

}
