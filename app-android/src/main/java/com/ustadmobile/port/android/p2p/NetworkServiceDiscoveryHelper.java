/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ustadmobile.port.android.p2p;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.sharedse.p2p.P2PNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;

import static android.content.Context.WIFI_SERVICE;
import static com.ustadmobile.port.android.p2p.DownloadTaskAndroid.DOWNLOAD_REQUEST_METHOD;

public class NetworkServiceDiscoveryHelper {

    private Context mContext;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener registrationListener;
    private static final String SERVICE_TYPE = "_http._tcp.";
    private static final String TAG = "NetworkDiscovery";
    private String mServiceName;
    private NsdServiceInfo mService;
    private boolean isFileLocallyAvailable;
    /**
     * Store list of all devices found having the same service (Our service)
     */
    private HashMap<String,NsdServiceInfo> foundServices=new HashMap<>();

    public static final String ACTION_NSD_SERVICE_REGISTERED ="nsd_registered";
    public static final String ACTION_NSD_NEW_DEVICE_FOUND ="nsd_device_found";
    public static final String EXTRA_NSD_DEVICE_IP_ADDRESS ="extra_nsd_ip_address";


    private boolean isDiscovering=false;

    public NetworkServiceDiscoveryHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager)mContext.getSystemService(Context.NSD_SERVICE);

    }

    /**
     * Initialize all listeners (Registration and Discovery)
     */
    public void initializeNsd() {
        initializeDiscoveryListener();
        initializeRegistrationListener();

    }


    public void setServiceName(String serviceName){
        this.mServiceName=serviceName;
    }

    /**
     * Initialize Network Service discovery registration listener
     */
    private void initializeRegistrationListener(){
        registrationListener=new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                Log.d(TAG,"Network Service Discovery Registered \n"+String.valueOf(NsdServiceInfo));
                mServiceName = NsdServiceInfo.getServiceName();
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_NSD_SERVICE_REGISTERED));
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.e(TAG,"Network Service Discovery registration failed: Error "+arg0);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG,"Network Service Discovery un-registration succeeded "+arg0.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG,"Network Service Discovery un-registration failed: Error "+errorCode);
            }

        };
    }

    /**
     * Initialize Network Discovery listener
     */
    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
                isDiscovering=true;
            }

            @Override
            public void onServiceFound(final NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(mServiceName)){
                    mNsdManager.resolveService(service, new NsdManager.ResolveListener() {

                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.e(TAG, "Resolve failed" + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.d(TAG, "Resolve Succeeded. " + serviceInfo);
                            foundServices.put(serviceInfo.getHost().getHostAddress(),serviceInfo);
                            P2PNode node=new P2PNode(serviceInfo.getHost().getHostAddress(),serviceInfo.getPort());
                            Intent localDevices=new Intent(ACTION_NSD_NEW_DEVICE_FOUND);
                            localDevices.putExtra(EXTRA_NSD_DEVICE_IP_ADDRESS,node.getNodeIPAddress());
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(localDevices);
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                isDiscovering=false;
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /**
     * register network services discovery
     */
    public void registerService() {

        try{
            NsdServiceInfo serviceInfo  = new NsdServiceInfo();
            String [] ipAddress=getDeviceIPAddress().replace(".","@").split("@");
            byte[] byteAddress = new byte[]
                    { (byte) Integer.parseInt(ipAddress[0]), (byte) Integer.parseInt(ipAddress[1]),
                            (byte) Integer.parseInt(ipAddress[2]), (byte) Integer.parseInt(ipAddress[3]) };

            InetAddress inetAddress = InetAddress.getByAddress(byteAddress);
            serviceInfo.setHost(inetAddress);
            int port=getLocalPort();
            serviceInfo.setPort(port);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);
            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get device IP Address
     * @return
     */
    public String getDeviceIPAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    /**
     * Start network Service Discovery
     */
    public void discoverServices() {
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    /**
     * Stop Network Service Discovery
     */
    public void stopDiscovery() {
        if(mDiscoveryListener!=null){
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    /**
     * Unregister registered service
     */
    public void tearDown() {
       if(registrationListener!=null){
           mNsdManager.unregisterService(registrationListener);
       }
    }

    /**
     * Get next available device port to be used for listening networking communication
     * @return
     */
    private int getLocalPort(){
        try{
            ServerSocket serverSocket =new ServerSocket(0);
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 9090;
    }

    /**
     * Check discovering task status
     * @return
     */
    public boolean isDiscovering(){
        return isDiscovering;
    }

    public HashMap<String,NsdServiceInfo> getFoundNsdServiceInfo(){
        return foundServices;
    }

    public boolean isFileAvailable(final String fileId, final String hostUrl){
        new AsyncTask<String,String,Boolean>(){

            @Override
            protected Boolean doInBackground(String... params) {
                boolean isAvailable=false;
               try{
                   String fileUri= UMFileUtil.joinPaths(new String[]{hostUrl,fileId});
                   URL url = new URL(fileUri);
                   URLConnection connection = url.openConnection();
                   HttpURLConnection httpConnection = (HttpURLConnection) connection;
                   int responseCode = httpConnection.getResponseCode();
                   isAvailable= responseCode==200;
               } catch (IOException e) {
                   e.printStackTrace();
               }
                return isAvailable;
            }

            @Override
            protected void onPostExecute(Boolean isAvailable) {
                super.onPostExecute(isAvailable);
                isFileLocallyAvailable=isAvailable;
            }
        }.execute();

        return isFileLocallyAvailable;
    }


}
