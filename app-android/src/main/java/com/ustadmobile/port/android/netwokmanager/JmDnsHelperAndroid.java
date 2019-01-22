package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * Created by mike on 9/6/17.
 */

public class JmDnsHelperAndroid implements ServiceListener, INsdHelperAndroid{

    private boolean discoveryEnabled = false;

    private boolean discoveryActive = false;

    private boolean broadcastEnabled = false;

    private boolean broadcastActive = false;

    private Context context;

    private WifiManager.MulticastLock multicastLock;

    private JmDNS jmDns;

    private NetworkManagerAndroid networkManager;

    private WifiManager wifiManager;

    public static final String MULTICAST_LOCK_TAG = "JmDns-UstadMobile";

    private static final String SERVICE_TYPE_SUFFIX = "._tcp.local.";

    private ServiceInfo localServiceInfo;

    private String networkServiceType;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean connected = info.isConnected();

            synchronized (JmDnsHelperAndroid.this) {
                if(connected && jmDns == null) {
                    int addr = wifiManager.getConnectionInfo().getIpAddress();
                    try {
                        final InetAddress inetAddr = InetAddress.getByAddress(new byte[]{
                                (byte)(addr & 0xFF),
                                (byte)((addr >> 8) & 0xFF),
                                (byte)((addr >> 16) & 0xFF),
                                (byte)((addr >> 24) & 0xFF)
                        });

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    jmDns = JmDNS.create(inetAddr);
                                    Log.i(NetworkManagerAndroid.TAG, "JmDnsHelper: JmDNS created on "
                                            + inetAddr.getHostAddress());
                                    if(discoveryEnabled)
                                        startNSDiscovery();

                                    if(broadcastEnabled)
                                        registerNSDService();
                                }catch(IOException e) {
                                    Log.e(NetworkManagerAndroid.TAG, "JmDnsHelper: Error creating JmDns", e);
                                }

                                return null;
                            }
                        }.execute();
                    }catch(IOException e) {
                        Log.e(NetworkManagerAndroid.TAG, "JmDnsHelper: Error creating JmDns", e);
                    }



                }else if(!connected && jmDns != null) {
                    try {
                        jmDns.removeServiceListener(networkServiceType + SERVICE_TYPE_SUFFIX,
                                JmDnsHelperAndroid.this);
                        jmDns.unregisterAllServices();
                        jmDns.close();
                        jmDns = null;
                        discoveryActive = false;
                        broadcastActive = false;
                        checkLock();
                    }catch(IOException e) {
                        Log.e(NetworkManagerAndroid.TAG, "JmDnsHelper: Exception removing service", e);
                    }
                }
            }
        }
    };

    public JmDnsHelperAndroid(Context context, NetworkManagerAndroid networkManager) {
        this.context = context;
        this.networkManager = networkManager;
        wifiManager = (WifiManager)context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        networkServiceType = UstadMobileSystemImpl.getInstance().getAppConfigString(
                AppConfig.KEY_NETWORK_SERVICE_TYPE, "_ustad", context);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public synchronized void startNSDiscovery() {
        discoveryEnabled = true;
        if(!discoveryActive && jmDns != null) {
            this.discoveryActive = true;
            checkLock();
            jmDns.addServiceListener(networkServiceType+ SERVICE_TYPE_SUFFIX,
                    this);
            Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: added service listener");
        }
    }

    @Override
    public synchronized void stopNSDiscovery() {
        discoveryEnabled = false;
        if(discoveryActive) {
            jmDns.removeServiceListener(networkServiceType + SERVICE_TYPE_SUFFIX,
                    this);
            discoveryActive = false;
            checkLock();
        }
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        String serviceName = event.getName();
        Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: service added: name :"
                + serviceName + " type: " + event.getType());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        ServiceInfo info = event.getInfo();
        Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: service removed: " + info.getName());
        networkManager.handleNetworkServiceRemoved(info.getName());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        ServiceInfo info = event.getInfo();
        Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: service resolved: " + info.getName()
                + " on " + TextUtils.join(", ", info.getHostAddresses())
                + "\n\t" + info);
        networkManager.handleNetworkServerDiscovered(info.getName(), info.getHostAddress(), info.getPort());
    }

    @Override
    public synchronized void registerNSDService() {
        broadcastEnabled = true;
        if(jmDns != null && !broadcastActive) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            String networkServiceName =
                    (btAdapter != null && btAdapter.getName() != null)
                    ? BluetoothAdapter.getDefaultAdapter().getName()
                            : networkServiceType + (int)(Math.random() * 5000);
            localServiceInfo = ServiceInfo.create(
                    networkServiceType + SERVICE_TYPE_SUFFIX, networkServiceName,
                    networkManager.getHttpListeningPort(), "path=/");
            try {
                jmDns.registerService(localServiceInfo );
                Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: service registered: "
                    + "type: " + localServiceInfo.getType() + " name: " + localServiceInfo.getName()
                    + " port: " + localServiceInfo.getPort());
                broadcastActive = true;
            }catch(IOException e) {
                Log.e(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: exception registering service", e);
            }
        }
    }

    @Override
    public synchronized void unregisterNSDService() {
        broadcastEnabled = false;
        if(broadcastActive && jmDns != null) {
            jmDns.unregisterService(localServiceInfo);
            Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: unregistered local service");
            broadcastActive = false;
            checkLock();
        }
    }

    public boolean isDiscoveringNetworkService() {
        return discoveryEnabled;
    }

    @Override
    public void onDestroy() {
        context.unregisterReceiver(mBroadcastReceiver);
    }

    private synchronized void checkLock() {
        if(multicastLock == null && jmDns != null && (discoveryEnabled || broadcastEnabled)) {
            multicastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_TAG);
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
            Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: Acquired multicast lock");
        }else if(multicastLock != null && (!discoveryEnabled && !broadcastEnabled)) {
            multicastLock.release();
            multicastLock = null;
            Log.i(NetworkManagerAndroid.TAG, "JmDnsHelperAndroid: Released multicast lock");
        }
    }

}
