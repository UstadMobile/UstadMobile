package com.ustadmobile.sharedse.network;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Helper class which handle WiFi P2P connection since it miss behaves when done with Kotlin
 *
 * **Observed Behavior*
 * When used kotlin to do the network connection, when we add network configuration it returns -1
 * as network ID also it won't be happy when doing reflection proxy
 *
 * **Expected Behavior*
 * Should be able to add network configuration to the network list and return it's ID also,
 * internal connect method using reflection should work.
 *
 * <b>WITH THIS, WiFi connection remain on Java</b>
 *
 * @author kileha3
 */
public class NetworkManagerBleHelper {

    private WifiManager wifiManager;

    private String ssid;

    private String passphrase;

    private int networkId = 0;

    /**
     * Constrictor used to create new instance of the NetworkManagerBleHelper
     * @param wifiManager active WifiManager
     */
    public NetworkManagerBleHelper(WifiManager wifiManager){
        this.wifiManager = wifiManager;
    }

    /**
     * Set P2P group information to be used for connection
     * @param ssid Group SSID
     * @param passphrase Group passphrase
     */
    public void setGroupInfo(String ssid, String passphrase){
        this.ssid = ssid;
        this.passphrase = passphrase;
    }

    private int getNetworkId(){
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

    /**
     * Connect to a given WiFi network. Here we are assuming that the security is WPA2 PSK as
     * per the WiFi Direct spec. In theory, it should be possible to leave these settings to
     * autodetect. In reality, we should specify these to reduce the chance of the connection
     * timing out.
     */
    public void enableWiFi(){
        try{
            networkId = getNetworkId();
            Class<?> actionLister = Class.forName("android.net.wifi.WifiManager$ActionListener");
            Object proxyInstance = Proxy.newProxyInstance(actionLister.getClassLoader(),
                    new Class[] {actionLister}, new NetworkManagerBle.WifiConnectInvocationProxyHandler());

            Method connectMethod = wifiManager.getClass().getMethod("connect",
                    int.class, actionLister);
            connectMethod.invoke(wifiManager,networkId,proxyInstance);

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

    /**
     * Disable currently connected network
     * @param isConnectedToWifi flag to indicate whether the device is currently connected to WiFi
     */
    public void disableNetwork(boolean isConnectedToWifi){
        //TODO: we must track any networks we successfully disable, so that we can re-enable them

        //This may or may not be allowed depending on the version of android we are using.
        //Sometimes Android will feel like reconnecting to the last network, even though we told
        //it what network to connect with.
        if(isConnectedToWifi && wifiManager.disconnect()){
            wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        }
    }

    public void removeNetwork(){
       if(networkId != -1){
           wifiManager.removeNetwork(networkId);
       }
    }
}
