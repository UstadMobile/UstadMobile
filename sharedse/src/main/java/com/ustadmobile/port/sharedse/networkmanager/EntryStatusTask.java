package com.ustadmobile.port.sharedse.networkmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.jar.Pack200;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class EntryStatusTask extends NetworkTask implements BluetoothConnectionHandler{

    private List<String> fileIds;

    private List<NetworkNode> knownNodes;

    private int currentNode;

    public EntryStatusTask(List<String> fileIds){
        this.fileIds=fileIds;
    }
    @Override
    public void start() {
        //THIS IS A SNAPSHOT: IT MIGHT CHANGE... HANDLE THIS SCENARIO
        knownNodes = networkManager.getKnownNodes();
        currentNode = 0;
        new Thread(new Runnable() {
            public void run() {
                connectNextNode();
            }
        }).start();
    }

    public void connectNextNode() {
        if(currentNode < knownNodes.size()) {
            String bluetoothAddr = knownNodes.get(currentNode).getDeviceBluetoothMacAddress();
            networkManager.connectBluetooth(bluetoothAddr, this);
        }else {
            networkManager.handleTaskCompleted(this);
        }
    }


    @Override
    public void cancel() {

    }

    @Override
    public void onConnected(InputStream inputStream, OutputStream outputStream) {
        String queryStr = BluetoothServer.CMD_ENTRY_STATUS_QUERY + ' ';
        for(int i = 0; i < fileIds.size(); i++){
            try { queryStr += URLEncoder.encode(fileIds.get(i), "UTF-8"); }
            catch(UnsupportedEncodingException e) {}//what device doesn't have UTF-8?

            if(i < fileIds.size() - 1)
                queryStr += ';';
        }

        queryStr += '\n';
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream.write(queryStr.getBytes());
            String response = reader.readLine();
            if(response.startsWith("200")) {

            }else {

            }
        }catch(IOException e) {

        }
    }

    @Override
    public int getQueueId() {
        return NetworkManager.QUEUE_ENTRY_STATUS;
    }

    @Override
    public int getTaskId() {
        return 0;
    }

    @Override
    public int getTaskType() {
        return 0;
    }
}
