package com.ustadmobile.port.android.impl.p2p

import com.ustadmobile.core.network.p2p.P2pManager
import android.content.Context
import android.content.Context.NSD_SERVICE
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.port.android.util.ext.getActivityContext
import io.github.aakira.napier.Napier
import java.net.InetAddress

class P2pManagerAndroid(val applicationContext: Context) : P2pManager {

    private var mServiceName = ""

    private lateinit var mService: NsdServiceInfo

    private lateinit var nsdManager: NsdManager

    private val SERVICE_TYPE = "_ustadapp._tcp"


    private val registrationListener = object: NsdManager.RegistrationListener{
        override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
            //Failed.
            Napier.d("P2PManager: onRegistration Failed! ")
        }

        override fun onUnregistrationFailed(p0: NsdServiceInfo?, p1: Int) {
            //Unreg failed.
            Napier.d("P2PManager: onUnregistration Failed! ")
        }

        override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
            mServiceName = nsdServiceInfo.serviceName
            Napier.d("P2PManagerAndroid: Registered ok: " + mServiceName)
        }

        override fun onServiceUnregistered(p0: NsdServiceInfo?) {
            //Un registered ok
            Napier.d("P2PManagerAndroid: Unregistered ok.")
        }
    }

    private val discoveryListener = object: NsdManager.DiscoveryListener{
        override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
            //failed to start discovery
            Napier.d("P2PManagerAndroid: onStartDiscoveryFailed !")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
            //failed on stop discovery
            Napier.d("P2PManagerAndroid: onStopDiscoveryFailed!")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onDiscoveryStarted(p0: String?) {
            //Discovery started
            Napier.d("P2PManagerAndroid: Discovery Started..")
        }

        override fun onDiscoveryStopped(p0: String?) {
            Napier.d("P2PManagerAndroid: Discovery Stopped.")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Napier.d("P2PManagerAndroid: onServiceFound: " + service.serviceName)
            Napier.d("P2PManagerAndroid: onServiceFound: "
                    + service.serviceName)

            when {
                !service.serviceType.startsWith(SERVICE_TYPE) -> {
                    Napier.d("Unknown Service Type: ${service.serviceType}")
                    Napier.d("P2PManagerAndroid: Unknown Service Type: " + service.serviceType)
                }
                service.serviceName == mServiceName -> {
                    Napier.d("Same machine: $mServiceName")
                    Napier.d( "P2PManagerAndroid: onServiceFound: "
                            + service.serviceName)
                }

                service.serviceName.contains("UstadP2P") -> {
                    nsdManager.resolveService(service, resolveListener)
                    Napier.d("P2PManagerAndroid: contains UstadP2P :" + service.serviceName
                            + " .. resolving..")
                }
            }
        }

        override fun onServiceLost(p0: NsdServiceInfo?) {
            Napier.d("P2PManagerAndroid: onServiceLost.")
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Napier.d( "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Napier.d( "Resolve Succeeded. $serviceInfo")

            if (serviceInfo.serviceName == mServiceName) {
                Napier.d( "Same IP.")
                Napier.d("P2PManagerAndroid: Same IP ")
                //return
            }
            mService = serviceInfo
            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host

            Napier.d("P2PManagerAndroid: host:port  "
                    + host + ":" + port)
            println("P2PManagerAndroid: host:port  "
                    + host + ":" + port)


        }
    }


    //Check the queue when something is added to watchlist, when a new node is discovered, or
    //when an executor finishes.

    /**
     * When a presenter or other component (e.g. ContentJobRunner) wants to monitor the status of
     * a given cotnainer, it should call this function. Availability information will be stored
     * in the database and accessed as LiveData or RateLimitedLiveData
     */
    override fun addToWatchList(contianerUids: List<Long>) {

        //1. When devices are discovered, add them to NetworkNode table

        //2. When containers discovered on devices (Query the devices list)

    }

    /**
     * Register the Network Service Discovery service on a specific port
     */
    override fun registerService(port: Int){

        val serviceInfo = NsdServiceInfo().apply{
            serviceName = "UstadP2P"
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        nsdManager = (applicationContext.getSystemService(NSD_SERVICE) as NsdManager).apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }


        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

    }

    override fun stop() {
        nsdManager.apply {
            unregisterService(registrationListener)
            stopServiceDiscovery(discoveryListener)
        }

    }
}