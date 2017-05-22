package com.ustadmobile.port.android.netwokmanager;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import static com.ustadmobile.core.buildconfig.CoreBuildConfig.NETWORK_SERVICE_NAME;
import static com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid.SERVICE_PORT;

/**
 * Created by kileha3 on 21/05/2017.
 */

public class NSDHelperAndroid {

    private NsdManager mNsdManager;
    private static final String SERVICE_TYPE = "_http._tcp.";
    private NsdServiceInfo nsdServiceInfo;
    private NetworkManagerAndroid managerAndroid;
    private boolean isDiscoveringNetworkService=false;

    private NsdManager.DiscoveryListener networkDiscoveryListener;
    private NsdManager.RegistrationListener networkRegistrationListener;

    public NSDHelperAndroid(NetworkManagerAndroid managerAndroid){
        this.managerAndroid=managerAndroid;
        mNsdManager = (NsdManager) managerAndroid.getContext().getSystemService(Context.NSD_SERVICE);

    }

    private void initializeServiceDiscoveryListener(){
        networkDiscoveryListener=new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(NetworkManagerAndroid.TAG, "Network Service discovery started");
                isDiscoveringNetworkService=true;
            }

            @Override
            public void onServiceFound(final NsdServiceInfo service) {
                Log.d(NetworkManagerAndroid.TAG, "Device found -> " + service.getServiceName()+" "+service.getHost());
                //Found the right service type, resolve it to get the appropriate details.
                mNsdManager.resolveService(service, new NsdManager.ResolveListener() {

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.d(NetworkManagerAndroid.TAG, "Network Service Resolved Successfully. " + serviceInfo);

                        managerAndroid.handleNetworkServerDiscovered(serviceInfo.getServiceName(),
                                serviceInfo.getHost().getHostAddress(),serviceInfo.getPort());
                    }

                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e(NetworkManagerAndroid.TAG, "Network Service Failed to resolve"+service.getServiceName() +" errorCode "+ errorCode);
                    }
                });
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(NetworkManagerAndroid.TAG, "Network Service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(NetworkManagerAndroid.TAG, "Network Service Discovery stopped");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(NetworkManagerAndroid.TAG, "Network Service Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(NetworkManagerAndroid.TAG, "Network Service Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }
    private void initializeServiceRegistrationListener(){
        networkRegistrationListener=new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                nsdServiceInfo=serviceInfo;
                Log.d(NetworkManagerAndroid.TAG,"Network Service discovery service registered successfully "+serviceInfo.getServiceName()+" port:"+serviceInfo.getPort());

            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInf, int error) {
                Log.e(NetworkManagerAndroid.TAG,"Network Service Discovery failed to register "+serviceInf.getServiceName()+" "+error);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };

    }

    /**
     * Register network service discovery service,
     * service which will be used to tell if two devices are on the same network.
     */

    public void registerNSDService(){

        if(networkRegistrationListener!=null){
            unregisterNSDService();
        }
        String networkServiceName=NETWORK_SERVICE_NAME+"NSD";
        initializeServiceRegistrationListener();
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(networkServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(SERVICE_PORT);
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD,networkRegistrationListener);
        Log.d(NetworkManagerAndroid.TAG,"Registering network service "+networkServiceName);
    }

    public void unregisterNSDService(){
        mNsdManager.unregisterService(networkRegistrationListener);
    }

    /**
     * Network service discovery process: After discovering a network service, it has to be resolved to get
     * extra information like IPAddress, Port number and Service Name
     */

    public void startNSDiscovery(){
        stopNSDiscovery();
        initializeServiceDiscoveryListener();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD,networkDiscoveryListener);
    }

    public void stopNSDiscovery(){
       if(networkDiscoveryListener!=null){
           mNsdManager.stopServiceDiscovery(networkDiscoveryListener);
           networkDiscoveryListener=null;
           nsdServiceInfo=null;
           isDiscoveringNetworkService=false;
       }
    }


    public NsdServiceInfo getNsdServiceInfo(){
        return nsdServiceInfo;
    }

    public boolean isDiscoveringNetworkService(){
        return isDiscoveringNetworkService;
    }

}
