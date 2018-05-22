package com.ustadmobile.port.android.util;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.HashMap;

import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 20/02/2017.
 */

public class P2PAndroidUtils {

    public static WifiDirectHandler wifiDirectHandler;
    public static final String DEVICE_IP_ADDRESS="192.168.49.1";
    public static final String DEVICE_MAC_ADDRESS="mac_address";
    public static final String FILE_FROM_TYPE="file_type_from";
    public static final String FILE_NAME="file_name";
    public static final String LAST_UPDATE_TIME="timestamp";
    public static final String FILE_PATH="file_path";
    public static final String SERVICE_START_DOWNLOADING="fileDownloadingStarted:P2PUtils";
    public static final String SERVICE_DOWNLOADING="fileDownloadingOnProgress:P2PUtils";
    public static final String SERVICE_DOWNLOADING_FINISHED="fileDownloadingFinished:P2PUtils";

    public HashMap<String,String> getPeerFileDetails(String key,Object context){

        String data=UstadMobileSystemImpl.getInstance().getAppPref(key,context);

        String [] details=data.split("/");

        if(details.length>2){

            HashMap<String,String> device=new HashMap<>();
            device.put(FILE_FROM_TYPE,details[0]);
            device.put(DEVICE_MAC_ADDRESS,details[1]);
            device.put(FILE_NAME,details[2]);
            device.put(FILE_PATH,details[3]);
            device.put(LAST_UPDATE_TIME,details[4]);

            return device;
        }

        return null;
    }
}
