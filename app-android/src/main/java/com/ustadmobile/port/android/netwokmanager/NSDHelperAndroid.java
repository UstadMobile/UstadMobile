package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;

import java.util.Vector;


/**
 * <h1>NSDHelperAndroid</h1>
 * This class is responsible for handling all network service discovery operations within UstadMobile app.
 * It will handle creating network service, discover other instances of the service and stop it when necessary.
 * This service was implemented as per Google's Android documentation.
 *
 * @link https://developer.android.com/training/connect-devices-wirelessly/nsd.html
 *
 * @author kileha3
 */

public class NSDHelperAndroid implements INsdHelperAndroid {

    private NsdManager mNsdManager;
    /**
     * Network service type which is the combination of Protocol and transport layer used.
     */
    private static final String SERVICE_TYPE = "_ustad._tcp.";

    private NsdServiceInfo nsdServiceInfo;
    private NetworkManagerAndroid managerAndroid;
    private boolean isDiscoveringNetworkService=false;



    private NSDHelperResolveQueueEntry currentEntry;

    private Vector<NSDHelperResolveQueueEntry> backlog = new Vector<>();


    private NsdManager.DiscoveryListener networkDiscoveryListener;
    private NsdManager.RegistrationListener networkRegistrationListener;


    private class NSDHelperResolveQueueEntry {

        private NsdServiceInfo serviceInfo;

        private int numAttempts;

        private NSDHelperResolveQueueEntry(NsdServiceInfo serviceInfo) {
            this.serviceInfo = serviceInfo;
        }

    }

    public NSDHelperAndroid(NetworkManagerAndroid managerAndroid){
        this.managerAndroid=managerAndroid;
    }

    private void lookupNsdManager() {
        if(mNsdManager == null)
            mNsdManager = (NsdManager) managerAndroid.getContext().getSystemService(Context.NSD_SERVICE);
    }

    private synchronized void queueResolveService(NsdServiceInfo serviceInfo) {
        backlog.addElement(new NSDHelperResolveQueueEntry(serviceInfo));
        checkQueue();
    }

    /**
     * Even though NsdManager.resolveService is asynchronous it will fail if something is already
     * in progress with FAILURE_ALREADY_ACTIVE.
     */
    private synchronized void checkQueue(){
        //remove previous entry if it has failed too many times
        if(currentEntry == null && backlog.size() > 0 && backlog.elementAt(0).numAttempts > 4) {
            Log.e(NetworkManagerAndroid.TAG, "Network Service: failed to resolve "
                + backlog.elementAt(0).serviceInfo + " after "
                + backlog.elementAt(0).numAttempts + " attempts.");
            backlog.removeElementAt(0);
        }

        if(currentEntry == null && backlog.size() > 0) {
            currentEntry = backlog.elementAt(0);
            currentEntry.numAttempts++;

            mNsdManager.resolveService(currentEntry.serviceInfo, new NsdManager.ResolveListener() {
                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.d(NetworkManagerAndroid.TAG, "Network Service Resolved Successfully. " + serviceInfo);

                    synchronized(NSDHelperAndroid.this) {
                        managerAndroid.handleNetworkServerDiscovered(serviceInfo.getServiceName(),
                                serviceInfo.getHost().getHostAddress(), serviceInfo.getPort());
                        backlog.removeElementAt(0);
                        currentEntry = null;
                        checkQueue();
                    }
                }

                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    Log.e(NetworkManagerAndroid.TAG, "Network Service Failed to resolve"
                            + serviceInfo.getServiceName() + "  error "
                            + getFailureReasonName(errorCode));
                    synchronized (NSDHelperAndroid.this) {
                        currentEntry = null;
                        checkQueue();
                    }
                }
            });
        }
    }




    /**
     * This method initialize all service discovery listeners,
     * once the service is found it will be resolved so that extra information
     * li   ke Host IP address and Service name can be obtained.
     *
     * <p>
     *     With this listener, different methods will be invoked on right events.
     *     <b>onDiscoveryStarted</b> :Invoked when service discovery start
     *     <b>onServiceFound</b> :Invoked when new peer device service is found
     *     <b>onServiceLost</b> :Invoked when new peer device service is lost
     *     <b>onDiscoveryStopped</b> :Invoked when discovery process is stopped
     *     <b>onStopDiscoveryFailed</b> :Invoked when discovery registration failed.
     * </p>
     *
     * <p>
     *     On resolving found services from other peer devices, two events might happen.
     *     <b>onServiceResolved</b>: Invoked when service resolved successfully
     *     <b>onResolveFailed</b>: Invoked when service failed to be resolved.
     * </p>
     */
    private void initializeServiceDiscoveryListener(){
        networkDiscoveryListener=new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(NetworkManagerAndroid.TAG, "Network Service discovery started");
                isDiscoveringNetworkService=true;
            }

            @Override
            public void onServiceFound(final NsdServiceInfo service) {
                Log.d(NetworkManagerAndroid.TAG, "serviceFound " + service.getServiceName()+" "+service.getHost());
                queueResolveService(service);
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.i(NetworkManagerAndroid.TAG, "Network Service lost: " + service.getServiceName()
                        + " from host " + service.getHost());
                managerAndroid.handleNetworkServiceRemoved(service.getServiceName());
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(NetworkManagerAndroid.TAG, "Network Service Discovery stopped");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(NetworkManagerAndroid.TAG, "Network Service Discovery failed: Error code:" +
                        getFailureReasonName(errorCode));
                //TODO: this should not be called
                //mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(NetworkManagerAndroid.TAG, "Network Service Discovery failed: Error code:" + errorCode);
                //TODO: This needs to retry stopping after an interval.
                //mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /**
     * This method initializes all service registration listeners
     * which include both registration and un-registration.
     * <p>
     *     Upon service registration, we do expect two different event outcome;-
     *     <b>onServiceRegistered</b>: This method will be called when service is successfully registered
     *     <b>onRegistrationFailed</b>: This method will be called when service registration failed
     *</p>
     *
     * <p>
     *     Upon un-registration of the service, we also do expect two outcomes events.
     *     <b>onServiceUnregistered</b>: This method will be called when service was unregistered successfully
     *     <b>onUnregistrationFailed</b>: This method will be called when service un-registration fails.
     * </p>
     *
     */
    private void initializeServiceRegistrationListener(){
        networkRegistrationListener=new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                nsdServiceInfo=serviceInfo;
                Log.d(NetworkManagerAndroid.TAG,"Network Service discovery service registered successfully:"
                        + " "+serviceInfo.getServiceName()+  "." + serviceInfo.getServiceType()
                        + " port:"+serviceInfo.getPort());

            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInf, int error) {
                Log.e(NetworkManagerAndroid.TAG,"Network Service Discovery failed to register "+serviceInf.getServiceName()+" "
                        +getFailureReasonName(error));
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d(NetworkManagerAndroid.TAG, "Network service discovery: successfully unregistered " + serviceInfo.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(NetworkManagerAndroid.TAG, "Network service discovery: failed to unregister "
                        + serviceInfo.getServiceName() + " error " + getFailureReasonName(errorCode));
            }

        };

    }

    /**
     * This method is responsible for registering the network services.
     * It will be invoked from NetworkManager, upon successful service registration
     * peer device will be abe to tell if the device belong to the same network.
     */

    public void registerNSDService(){

        if(networkRegistrationListener!=null){
            unregisterNSDService();
        }

        String networkServiceName = BluetoothAdapter.getDefaultAdapter() != null
                    ? BluetoothAdapter.getDefaultAdapter().getName()
                    : CoreBuildConfig.NETWORK_SERVICE_TYPE + (int)(Math.random() * 5000);
        initializeServiceRegistrationListener();
        final NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(networkServiceName);
        serviceInfo.setServiceType(CoreBuildConfig.NETWORK_SERVICE_TYPE + "._tcp");
        serviceInfo.setPort(managerAndroid.getHttpListeningPort());
        lookupNsdManager();
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD,networkRegistrationListener);
        Log.d(NetworkManagerAndroid.TAG,"Registering network service "+networkServiceName);
    }

    /**
     * This method will be handling un-registration part of the network service.
     * Upon un-registration of the listener which registered the service, service will stop.
     */
    public void unregisterNSDService(){
        if(networkRegistrationListener != null) {
            mNsdManager.unregisterService(networkRegistrationListener);
            networkRegistrationListener = null;
        }
    }

    /**
     * Start network service discovery process:
     */

    public void startNSDiscovery(){
        Log.i(NetworkManagerAndroid.TAG, "NSDHelperAndroid: start NS discovery");
        stopNSDiscovery();
        initializeServiceDiscoveryListener();
        lookupNsdManager();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD,networkDiscoveryListener);
    }

    /**
     * This method is responsible for stopping all service discovery operations.
     * After calling it, no other peer device services will be discovered
     */
    public synchronized void stopNSDiscovery(){
       if(mNsdManager != null && networkDiscoveryListener!=null){
           mNsdManager.stopServiceDiscovery(networkDiscoveryListener);
           networkDiscoveryListener=null;
           nsdServiceInfo=null;
           isDiscoveringNetworkService=false;
       }
    }

    /**
     *
     * @return NsdServiceInfo: Network service discovery information.
     *                         This provide extra information about the service.
     */
    public NsdServiceInfo getNsdServiceInfo(){
        return nsdServiceInfo;
    }

    /**
     *
     * @return boolean: This returns logical answer on whether the service is currently active or not.
     *                  TRUE: When the service is active
     *                  FALSE: When the service is inactive
     */
    public boolean isDiscoveringNetworkService(){
        return isDiscoveringNetworkService;
    }

    public static String getFailureReasonName(int error) {
        switch(error) {
            case NsdManager.FAILURE_ALREADY_ACTIVE:
                return "FAILURE_ALREADY_ACTIVE";

            case NsdManager.FAILURE_INTERNAL_ERROR:
                return "FAILURE_INTERNAL_ERROR";

            case NsdManager.FAILURE_MAX_LIMIT:
                return "FAILURE_MAX_LIMIT";
        }

        return "UNKNOWN";
    }

    @Override
    public void onDestroy() {

    }
}
