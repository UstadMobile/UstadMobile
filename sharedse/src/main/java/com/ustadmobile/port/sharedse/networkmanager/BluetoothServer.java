package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.UUID;

/**
 * Created by kileha3 on 09/05/2017.
 */

public abstract class BluetoothServer implements WiFiDirectGroupListener{

    public static final UUID SERVICE_UUID = UUID.fromString("ad9e3a05-7d80-4a12-b50b-91c72d442683");

    public static final String SERVICE_NAME = "UstadMobileBT";

    /**
     * Entry status command. The client sends one line commands. For status the command is STATUS
     * followed by each entry id (URL encoded) separated by ;
     *
     * STATUS id1;id2;id3;
     * Reply:
     * 200 0;1;0
     *
     * Where 1 indicates the entry id in that position is available, 0 indicates it is not available
     *
     */
    public static final String CMD_ENTRY_STATUS_QUERY = "STATUS";


    /**
     *
     * ACQUIRE id1;id2;id3
     */
    public static final String CMD_ACQUIRE_ENTRY = "ACQUIRE";

    public static final String CMD_SEPARATOR =";";

    public static final String CMD_ENTRY_STATUS_FEEDBACK = "STATUS_FEEDBACK";
    public static final String CMD_ACQUIRE_ENTRY_FEEDBACK = "ACQUIRE_FEEDBACK";

    private NetworkManager networkManager;

    private final Object bluetoothLock=new Object();

    public static final int GROUP_INFO_AVAILABLE_WAITING_TIME =60 * 1000;

    public BluetoothServer(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }



    public abstract void start();
    public abstract void stop();

    public void handleNodeConnected(String deviceAddress, InputStream inputStream,
                                    final OutputStream outputStream) throws IOException{

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String clientInput = reader.readLine();

        if(clientInput.startsWith(CMD_ENTRY_STATUS_QUERY)) {
            System.out.print("BluetoothSReceived command "+clientInput);
            String[] entryIds = clientInput.substring(CMD_ENTRY_STATUS_QUERY.length()+1).split(";");
            boolean[] results = new boolean[entryIds.length];
            CatalogEntryInfo info;
            String response = CMD_ENTRY_STATUS_FEEDBACK+" ";
            for(int i = 0; i < entryIds.length; i++) {
                info = CatalogController.getEntryInfo(URLDecoder.decode(entryIds[i], "UTF-8"),
                        CatalogController.SHARED_RESOURCE, networkManager.getContext());
                results[i] = info != null && info.acquisitionStatus == CatalogController.STATUS_ACQUIRED;
                response += results[i] ? '1' : '0';
                if(i < entryIds.length - 1)
                    response += CMD_SEPARATOR;
            }
            response += '\n';
            System.out.print("Sending response "+response);
            outputStream.write(response.getBytes());
            outputStream.flush();
            UMIOUtils.closeOutputStream(outputStream);
            UMIOUtils.closeInputStream(inputStream);
        }

        if(clientInput.startsWith(CMD_ACQUIRE_ENTRY)) {
            System.out.print("BluetoothSReceived command "+clientInput);
            WiFiDirectGroup group=networkManager.getWifiDirectGroup();
            networkManager.addWifiDirectGroupListener(this);
           if(group!=null){
              writeToStream(outputStream,group);
           }else{
               networkManager.createWifiDirectGroup();
               synchronized (bluetoothLock){
                   try {
                       bluetoothLock.wait(GROUP_INFO_AVAILABLE_WAITING_TIME);
                       writeToStream(outputStream,networkManager.getWifiDirectGroup());
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

               }

           }
        }
    }


    private void writeToStream(OutputStream outputStream,WiFiDirectGroup group){
        try{
            String acquireFeedback=CMD_ACQUIRE_ENTRY_FEEDBACK+" "+group.getSsid()+CMD_SEPARATOR
                    +group.getPassphrase()+CMD_SEPARATOR+networkManager.getWifiDirectIpAddress()+"\n";
            outputStream.write(acquireFeedback.getBytes());
            outputStream.flush();
            System.out.print("Sending response "+acquireFeedback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void groupCreated(WiFiDirectGroup group, Exception err) {
        synchronized (bluetoothLock){
            bluetoothLock.notifyAll();
        }
    }

    @Override
    public void groupRemoved(boolean successful, Exception err) {

    }
}
