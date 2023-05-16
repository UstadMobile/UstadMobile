package com.ustadmobile.sharedse.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ustadmobile.core.impl.UMAndroidUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.sharedse.network.NetworkManagerBleCommon.WIFI_DIRECT_GROUP_SSID_PREFIX;


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

    /**
     * This class is used when creating a ActionListener proxy to be used on
     * WifiManager#connect(int,ActionListener) invocation through reflection.
     */
    public class WifiConnectInvocationProxyHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            Log.d("Invocation","Method was invoked using reflection  "+method.getName());
            return null;
        }
    }

    private WifiManager wifiManager;

    private ConnectivityManager connectivityManager;

    private final List<String> temporaryWifiDirectSsids = new ArrayList<>();

    private String ssid;

    private String passphrase;

    private int lastNetworkIdAdded = -1;


    /**
     * Constrictor used to create new instance of the NetworkManagerBleHelper
     * @param context Application context
     */
    public NetworkManagerBleHelper(Context context){
        if(wifiManager == null){
            wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
        }

        if(connectivityManager == null){
            connectivityManager = (ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }

    /**
     * Get current active wifimanager
     * @ wifimanager object
     */
    public WifiManager getWifiManager() {
        return wifiManager;
    }

    /**
     * Get instance of an active connectivity manager
     * @return connectivity manager object
     */
    public ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

    /**
     * Set P2P group information to be used for connection
     * @param ssid Group SSID
     * @param passphrase Group passphrase
     */
    public void setGroupInfo(String ssid, String passphrase){
        this.ssid = ssid;
        this.passphrase = passphrase;
        lastNetworkIdAdded = -1;

        if (ssid.startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX)) {
            temporaryWifiDirectSsids.add(ssid);
        }
    }

    /**
     * Add new newtwork configuration to the device configuration list
     * @return newly added nwtwork id
     */
    public int addNetwork(){
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\""+ ssid +"\"";
        config.preSharedKey = "\""+ passphrase +"\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        /* If the config does not indicate that the network is hidden, Android will only attempt to
         * connect once it sees the network. Because the network was likely only just create by the
         * peer device, this would cause a delay.
         */
        config.hiddenSSID = true;
        final int networkId = wifiManager.addNetwork(config);
        lastNetworkIdAdded = networkId;
        return networkId;
    }


    public int getLastNetworkIdAdded(){

        //This is no longer used

        return -1;
    }

    /**
     * Delete all app's specific networks from the list of all congifured device networks
     */
    public void deleteTemporaryWifiDirectSsids(){
        if (temporaryWifiDirectSsids.isEmpty())
            return;

    }

    /**
     * Connect to a given WiFi network. Here we are assuming that the security is WPA2 PSK as
     * per the WiFi Direct spec. In theory, it should be possible to leave these settings to
     * autodetect. In reality, we should specify these to reduce the chance of the connection
     * timing out.
     *
     * @return true if the enableNetwork call was made successfully (without exceptions), false otherwise
     */
    public boolean enableWifiNetwork(){
        if(isConnectedToWifi()) {
            disableCurrentWifiNetwork();
        }

        try{
            Class<?> actionLister = Class.forName("android.net.wifi.WifiManager$ActionListener");
            Object proxyInstance = Proxy.newProxyInstance(actionLister.getClassLoader(),
                    new Class[] {actionLister}, new WifiConnectInvocationProxyHandler());

            Method connectMethod = wifiManager.getClass().getMethod("connect",
                    int.class, actionLister);
            final int networkId = lastNetworkIdAdded == -1 ? addNetwork() : lastNetworkIdAdded;
            connectMethod.invoke(wifiManager, networkId ,proxyInstance);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Disable currently connected network
     */
    private void disableCurrentWifiNetwork(){
        //TODO: we must track any networks we successfully disable, so that we can re-enable them

        //This may or may not be allowed depending on the version of android we are using.
        //Sometimes Android will feel like reconnecting to the last network, even though we told
        //it what network to connect with.
        if(isConnectedToWifi() && wifiManager.disconnect()){
            wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        }
    }

    /**
     * @return true if is connected on WiFi otherwise false
     */
    private boolean isConnectedToWifi() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        return info != null
                && info.getType() == ConnectivityManager.TYPE_WIFI
                && info.isConnected();
    }

    /**
     * Restore previously connected wifi network
     */
    public void restoreWiFi(){
        wifiManager.disconnect();
        deleteTemporaryWifiDirectSsids();
        wifiManager.reconnect();
    }
}
