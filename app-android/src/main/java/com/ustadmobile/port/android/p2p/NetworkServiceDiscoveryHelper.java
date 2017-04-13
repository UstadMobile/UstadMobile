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
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.WIFI_SERVICE;

public class NsdHelper {

    private Context mContext;

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private UpdateDeviceFoundInterface updateDeviceFoundInterface;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = "UstadNsd";
    private NsdServiceInfo mService;

    public static final String DEVICE_IP_ADDRESS="device_address";
    public static final String DEVICE_PORT_NUMBER="device_port";
    private ArrayList<HashMap<String,String>> foundDevices=new ArrayList<>();
    public NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

    }

    public void initializeNsd() {
        initializeDiscoveryListener();
        updateDeviceFoundInterface= (UpdateDeviceFoundInterface) mContext;

    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
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
                            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                            HashMap<String,String> device=new HashMap<>();
                            device.put(DEVICE_IP_ADDRESS,serviceInfo.getHost().getHostAddress());
                            device.put(DEVICE_PORT_NUMBER,String.valueOf(serviceInfo.getPort()));
                            if(!foundDevices.contains(device)){
                                foundDevices.add(device);
                                updateDeviceFoundInterface.onDeviceFound(foundDevices);
                            }

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


    public void registerService(int port) {

        try{
            NsdServiceInfo serviceInfo  = new NsdServiceInfo();
            String [] ipAddress=getDeviceIPAddress().replace(".","@").split("@");
            Log.d(MainActivity.TAG,"Device IP: "+ipAddress);
            byte[] byteAddress = new byte[]
                    { (byte) Integer.parseInt(ipAddress[0]), (byte) Integer.parseInt(ipAddress[1]),
                            (byte) Integer.parseInt(ipAddress[2]), (byte) Integer.parseInt(ipAddress[3]) };

            InetAddress inetAddress = InetAddress.getByAddress(byteAddress);
            serviceInfo.setHost(inetAddress);
            serviceInfo.setPort(port);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);
            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, new NsdManager.RegistrationListener() {

                        @Override
                        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                            mServiceName = NsdServiceInfo.getServiceName();
                        }

                        @Override
                        public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                        }

                        @Override
                        public void onServiceUnregistered(NsdServiceInfo arg0) {
                        }

                        @Override
                        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        }

                    });
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
    public String getDeviceIPAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    public void discoverServices() {
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopDiscovery() {
        if(mDiscoveryListener!=null){
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }
    public void tearDown() {
       if(mRegistrationListener!=null){
           mNsdManager.unregisterService(mRegistrationListener);
       }
    }


    public interface UpdateDeviceFoundInterface{
        void onDeviceFound(ArrayList<HashMap<String, String>> devices);
    }
}
