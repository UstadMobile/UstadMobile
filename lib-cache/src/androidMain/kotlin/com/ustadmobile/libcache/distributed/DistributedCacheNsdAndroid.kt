package com.ustadmobile.libcache.distributed

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.logging.UstadCacheLogger

class DistributedCacheNsdAndroid(
    context: Context,
    private val port: Int,
    private val logger: UstadCacheLogger,
    private val listener: DistributedCacheNeighborDiscoveryListener,
) {

    /**
     * The currently registered service name
     */
    private var mServiceName: String? = SERVICE_NAME

    /**
     * host will be null if info not yet resolved
     */
    val NsdServiceInfo.neighborUrl: String?
        get() = host?.let { "http://${it.hostName}:${port}/" }

    val serviceInfo = NsdServiceInfo().apply {
        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceName = SERVICE_NAME
        serviceType = SERVICE_TYPE
        setPort(this@DistributedCacheNsdAndroid.port)
    }

    //as per https://developer.android.com/develop/connectivity/wifi/use-nsd
    private val registrationListener = object: NsdManager.RegistrationListener {

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            mServiceName = serviceInfo.serviceName
            logger.i(DCACHE_LOGTAG, "Registered: $serviceInfo")

            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            logger.i(DCACHE_LOGTAG, "Unregistered: $serviceInfo")
            nsdManager.stopServiceDiscovery(discoveryListener)
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            logger.e(DCACHE_LOGTAG, "Registered failed: $serviceInfo : $errorCode")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            logger.e(DCACHE_LOGTAG, "Unregister failed: $serviceInfo : $errorCode")
        }

    }

    private val nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager)


    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            logger.e(DCACHE_LOGTAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            logger.i(DCACHE_LOGTAG, "Resolve Succeeded. $serviceInfo")

            if (serviceInfo.serviceName == mServiceName) {
                logger.d(DCACHE_LOGTAG, "Same IP.")
                return
            }

            val neighborUrlVal = serviceInfo.neighborUrl
            if(neighborUrlVal != null) {
                listener.onNeighborDiscovered(neighborUrlVal)
            }else {
                logger.e(DCACHE_LOGTAG, "Error: could not get neighborUrl. Url should not " +
                        "have been null after service resolved")
            }


        }
    }

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            logger.d(DCACHE_LOGTAG, "Service discovery started")
        }

        /**
         *
         * When on serviceFound is hit: Seems to add a . - no docs, thanks Google.
         *
         * @param other service type (without trailing .)
         */
        fun String.serviceTypeMatches(other: String): Boolean {
            return this == other || this == "${other}."
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            logger.d(DCACHE_LOGTAG, "Service discovery onServiceFound: $service")
            when {
                !service.serviceType.serviceTypeMatches(SERVICE_TYPE) -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    logger.d(DCACHE_LOGTAG, "Unknown Service Type: ${service.serviceType}")
                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    logger.d(DCACHE_LOGTAG, "Same machine: $mServiceName")
                service.serviceName.contains(SERVICE_NAME) -> nsdManager.resolveService(service, resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            logger.e(DCACHE_LOGTAG, "service lost: $service")
            val neighborUrlVal = service.neighborUrl
            if(neighborUrlVal != null) {
                listener.onNeighborLost(neighborUrlVal)
            }else {
                logger.d(DCACHE_LOGTAG, "Service lost, but neighbor url is null")
            }
        }

        override fun onDiscoveryStopped(serviceType: String) {
            logger.i(DCACHE_LOGTAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            logger.e(DCACHE_LOGTAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            logger.e(DCACHE_LOGTAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    init {
        /* To avoid discovering the device itself, Android as per docs relies on comparing the
         * discovered service name with the registered service name.
         *
         * Therefor: we first register the service, get the name registered, and then the
         * registrationListener's onServiceRegistered function will start discovery, so when
         * onServiceFound is called we can compare the found service name with the registered
         * service name.
         */
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }


    companion object {

        const val SERVICE_NAME = "DCache"

        const val SERVICE_TYPE = "_dcache._tcp"

    }


}