package com.ustadmobile.port.sharedse.networkmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class EntryStatusTask extends NetworkTask implements BluetoothConnectionHandler{

    private List<String> entryIdList;
    private static final String FILE_ID_SEPARATOR=";";
    private List<NetworkNode> networkNodeList;

    private int currentNode;

    public EntryStatusTask(List<String> entryIdList, List<NetworkNode> networkNodeList, NetworkManager networkManager){
        super(networkManager);
        this.entryIdList = entryIdList;
        this.networkNodeList = networkNodeList;
    }
    @Override
    public void start() {
        currentNode = 0;
        new Thread(new Runnable() {
            public void run() {
                connectNextNode();
            }
        }).start();
    }

    public void connectNextNode() {
        if(currentNode < networkNodeList.size()) {
            String bluetoothAddr = networkNodeList.get(currentNode).getDeviceBluetoothMacAddress();
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
        List<Boolean> entryIdStatusList=new ArrayList<>();
        for(int i = 0; i < entryIdList.size(); i++){
            try { queryStr += URLEncoder.encode(entryIdList.get(i), "UTF-8"); }
            catch(UnsupportedEncodingException e) {}//what device doesn't have UTF-8?

            if(i < entryIdList.size() - 1)
                queryStr += FILE_ID_SEPARATOR;
        }

        queryStr += '\n';
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream.write(queryStr.getBytes());
            String response = reader.readLine();
            if(response.startsWith(BluetoothServer.CMD_ENTRY_STATUS_FEEDBACK)) {
                response=response.substring((BluetoothServer.CMD_ENTRY_STATUS_FEEDBACK.length()+1),response.length());

                for(String status: response.split(FILE_ID_SEPARATOR)){
                    boolean responseStatus= status.equals("1");
                    entryIdStatusList.add(responseStatus);
                }

                networkManager.handleEntriesStatusUpdate(networkNodeList.get(currentNode), entryIdList,entryIdStatusList);
                managerTaskListener.handleTaskCompleted(this);
                currentNode++;

            }else {
                System.out.print("Feedback "+response);
            }
        }catch(IOException e) {
            e.printStackTrace();
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
