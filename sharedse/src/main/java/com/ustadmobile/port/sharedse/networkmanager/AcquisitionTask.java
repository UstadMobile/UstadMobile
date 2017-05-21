package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class AcquisitionTask extends NetworkTask implements BluetoothConnectionHandler{

    private UstadJSOPDSFeed feed;
    protected NetworkManagerTaskListener listener;
    private int currentEntryId;


    public AcquisitionTask(UstadJSOPDSFeed feed,NetworkManager networkManager){
        super(networkManager);
        this.feed=feed;
    }

    /**
     * Start the download task
     */
    public synchronized void start() {
        currentEntryId=0;

        new Thread(new Runnable() {
            @Override
            public void run() {

                acquireNextFile();
            }
        }).start();
    }

    private void acquireNextFile(){
        long downloadId= new AtomicInteger().incrementAndGet();
        if(currentEntryId < feed.entries.length) {
            String entryId = feed.entries[currentEntryId].id;

            List<EntryCheckResponse> entryCheckResponseList = networkManager.getEntryResponses().get(entryId);

            if (entryCheckResponseList != null) {
                EntryCheckResponse entryCheckResponse = getResponseWithEntry(entryCheckResponseList);

                //File can be downloaded locally
                boolean areOnSameNetwork = areOnSameNetwork(
                        entryCheckResponse.getNetworkNode().getDeviceIpAddress());
                if (areOnSameNetwork) {
                    //Download from the peer on the same network,
                    // if connection can't be made fall back to bluetooth
                    acquireEntry(NetworkManager.DOWNLOAD_SOURCE_PEER_SAME_NETWORK
                            , feed.entries[currentEntryId].id, downloadId);
                } else {
                    //Start bluetooth handshakes to create no prompt network
                    acquireEntry(NetworkManager.DOWNLOAD_SOURCE_PEER_DIFFERENT_NETWORK
                            , feed.entries[currentEntryId].id, downloadId);
                }

            } else {

                acquireEntry(NetworkManager.DOWNLOAD_SOURCE_CLOUD,feed.entries[currentEntryId].id,downloadId);
            }
        }else{
            networkManager.handleTaskCompleted(this);
        }
    }


    private void acquireEntry(int source,String entry,long downloadId){
        networkManager.handleFileAcquisitionInformationAvailable(entry,downloadId,source);
        currentEntryId++;
        acquireNextFile();
    }

    private EntryCheckResponse getResponseWithEntry(List<EntryCheckResponse> responseList){
        if(responseList!=null &&!responseList.isEmpty()){
            for(EntryCheckResponse response: responseList){
                if(response.isFileAvailable()){
                    return response;
                }
            }
        }
        return null;
    }

    private boolean areOnSameNetwork(String server_ip){

        if(networkManager.getDeviceIPAddress()!=null){
            String serverDeviceAddress=server_ip.split("\\.")[0]+server_ip.split("\\.")[1]
                    +server_ip.split("\\.")[2];
            String clientDeviceIPAddress=networkManager.getDeviceIPAddress().split("\\.")[0]
                    +networkManager.getDeviceIPAddress().split("\\.")[1]
                    +networkManager.getDeviceIPAddress().split("\\.")[2];
            return serverDeviceAddress.equals(clientDeviceIPAddress);
        }else{
            return false;
        }


    }

    @Override
    public void onConnected(InputStream inputStream, OutputStream outputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String command = BluetoothServer.CMD_ACQUIRE_ENTRIES;

    }

    @Override
    public void cancel() {

    }

    @Override
    public int getQueueId() {
        return this.queueId;
    }

    @Override
    public int getTaskId() {
        return this.taskId;
    }

    @Override
    public int getTaskType() {
        return this.taskType;
    }

    public synchronized boolean stop(){
        return false;
    }

    public UstadJSOPDSFeed getFeed() {
        return feed;
    }

    public void setFeed(UstadJSOPDSFeed feed) {
        this.feed = feed;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AcquisitionTask && getFeed().equals(this.feed);
    }
}
