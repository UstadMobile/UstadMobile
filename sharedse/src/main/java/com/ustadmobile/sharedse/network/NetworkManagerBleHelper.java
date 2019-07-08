package com.ustadmobile.sharedse.network;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class UmP2pConnectivityManager {

    private static int getNetworkId(WifiManager wifiManager, String ssid, String passphrase){
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\""+ ssid +"\"";
        config.preSharedKey = "\""+ passphrase +"\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return wifiManager.addNetwork(config);

    }

    public static void enableWiFi(WifiManager wifiManager, String ssid, String passphrase){
        try{
            int netId = getNetworkId(wifiManager,ssid,passphrase);
            Class<?> actionLister = Class.forName("android.net.wifi.WifiManager$ActionListener");
            Object proxyInstance = Proxy.newProxyInstance(actionLister.getClassLoader(),
                    new Class[] {actionLister}, new NetworkManagerBle.WifiConnectInvocationProxyHandler());

            Method connectMethod = wifiManager.getClass().getMethod("connect",
                    int.class, actionLister);
            connectMethod.invoke(wifiManager,netId,proxyInstance);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
