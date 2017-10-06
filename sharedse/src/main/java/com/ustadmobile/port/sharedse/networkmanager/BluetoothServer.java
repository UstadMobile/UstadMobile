package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.UUID;

/**
 * <h1>BluetoothServer</h1>
 *
 * This is a cross platform class which handles all bluetooth connections,
 * It is responsible to send and received commands depending on whether the
 * device is in client or super node mode.
 *
 * @see com.ustadmobile.port.sharedse.networkmanager.WiFiDirectGroupListener
 *
 * @author kileha3
 */

public abstract class BluetoothServer implements WiFiDirectGroupListener{

    /**
     * Application universally unique identifier for bluetooth connections.
     */
    public static final UUID SERVICE_UUID = UUID.fromString("ad9e3a05-7d80-4a12-b50b-91c72d442683");

    /**
     * Application bluetooth name.
     */
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
     *String command tag, indicate that the stream carry Entry acquisition request.
     * When server side receive this, it will create a group and send back group
     * information (SSID and passphrase)
     */
    public static final String CMD_ACQUIRE_ENTRY = "ACQUIRE";

    /**
     * Separators which used to separate entry ID's when represented as String.
     */
    public static final String CMD_SEPARATOR =";";

    /**
     * String command tag, indicates that the stream carry feedback of entry status
     */
    public static final String CMD_ENTRY_STATUS_FEEDBACK = "STATUS_FEEDBACK";

    /**
     * String command tag, indicated that the stream carry feedback of entry
     * acquisition which is Wi-Fi Direct Group SSI and Passphrase
     */
    public static final String CMD_ACQUIRE_ENTRY_FEEDBACK = "ACQUIRE_FEEDBACK";

    /**
     * String command tag that indicates an error has occurred
     */
    public static final String CMD_ERROR_RESPONSE = "ERROR";

    private NetworkManager networkManager;

    private final Object bluetoothLock=new Object();

    /**
     * Maximum time to wait for the group information after creating a group.
     */
    public static final int GROUP_INFO_AVAILABLE_WAITING_TIME =60 * 1000;

    public BluetoothServer(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    /**
     * Method which is responsible to start all bluetooth operations.
     */
    public abstract void start();

    /**
     * Method which is responsible to stop all bluetooth operations.
     */
    public abstract void stop();

    /**
     * Method invoked when bluetooth connection is successfully made.
     * @param deviceAddress Peer bluetooth address which will be talking to it.
     * @param inputStream InputStream to read data from (Commands and data sent
     *                    from peer device)
     * @param outputStream OutputStream to write data on to peer device
     * @throws IOException
     */
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
                info = CatalogPresenter.getEntryInfo(URLDecoder.decode(entryIds[i], "UTF-8"),
                        CatalogPresenter.SHARED_RESOURCE, networkManager.getContext());
                results[i] = info != null && info.acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED;
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
                       group = networkManager.getWifiDirectGroup();
                       if(group != null) {
                           writeToStream(outputStream, group);
                       }else {
                           outputStream.write(CMD_ERROR_RESPONSE.getBytes("UTF-8"));
                           outputStream.flush();
                       }

                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

               }

           }
        }
    }

    /**
     * Method to write data to the stream as Entry acquisition feedback
     * @param outputStream OutputStream to write to.
     * @param group WiFiDirectGroup information.
     */
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

    /**
     * @param group The group created or null if it was not created due to an error
     * @param err The exception if any occurred attempting to create the group,
     */
    @Override
    public void groupCreated(WiFiDirectGroup group, Exception err) {
        synchronized (bluetoothLock){
            bluetoothLock.notifyAll();
        }
    }

    /**
     *
     * @param successful True if the group was successfully removed
     * @param err The exception if any
     */

    @Override
    public void groupRemoved(boolean successful, Exception err) {

    }
}
