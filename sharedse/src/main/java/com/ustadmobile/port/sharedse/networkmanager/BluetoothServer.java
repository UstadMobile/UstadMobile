package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;

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

public abstract class BluetoothServer {

    public static final int BLUETOOTH_STATE_NONE=-1;
    public static final int BLUETOOTH_STATE_CONNECTING=1;
    public static final int BLUETOOTH_STATE_CONNECTED=2;
    public static final int BLUETOOTH_STATE_FAILED =0;

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

    public static final String CMD_PING = "PING";


    /**
     *
     * ACQUIRE id1;id2;id3
     */
    public static final String CMD_ACQUIRE_ENTRIES = "ACQUIRE";

    protected NetworkManager networkManager;

    public BluetoothServer(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }



    public abstract void start();
    public abstract void stop();

    public void handleNodeConnected(String deviceAddress,InputStream inputStream,
                                             OutputStream outputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String clientInput = reader.readLine();
        if(clientInput.startsWith(CMD_ENTRY_STATUS_QUERY)) {
            String[] entryIds = clientInput.substring(CMD_ENTRY_STATUS_QUERY.length()+1).split(";");
            boolean[] results = new boolean[entryIds.length];
            CatalogEntryInfo info;
            String response = "200 ";
            for(int i = 0; i < entryIds.length; i++) {
                info = CatalogController.getEntryInfo(URLDecoder.decode(entryIds[i], "UTF-8"),
                        CatalogController.SHARED_RESOURCE, networkManager.getContext());
                results[i] = info != null && info.acquisitionStatus == CatalogController.STATUS_ACQUIRED;
                response += results[i] ? '1' : '0';
                if(i < entryIds.length - 1)
                    response += ';';
            }
            response += '\n';
            outputStream.write(response.getBytes());
        }else if(clientInput.startsWith(CMD_ACQUIRE_ENTRIES)) {

        }
    }

}
