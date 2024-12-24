package com.ustadmobile.libcache.distributed

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * This is mostly as per as per https://developer.android.com/develop/connectivity/wifi/use-nsd
 *
 * With a few exceptions:
 *  a) we do not rely on checking the service name in onServiceResolved with the registered service
 *     name because this creates a callback race; e.g. if the discovery starts before being registered.
 *     Instead we check the IP address when resolved.
 *
 *  b) When calling resolve in onServiceFound we create a new class. If resolveService is called
 *     twice using the same listener (before the first resolve call has completed), this leads to
 *     an exception being thrown.
 */
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

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val serviceInfo = NsdServiceInfo().apply {
        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceName = SERVICE_NAME
        serviceType = SERVICE_TYPE
        setPort(this@DistributedCacheNsdAndroid.port)
    }

    private val registrationListener = object: NsdManager.RegistrationListener {

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            mServiceName = serviceInfo.serviceName
            logger.i(DCACHE_LOGTAG, "Registered: $serviceInfo")
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

    inner class ResolveListener: NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            logger.e(DCACHE_LOGTAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            logger.i(DCACHE_LOGTAG, "Resolve Succeeded. $serviceInfo")

            val neighborHostAddr = serviceInfo.host
            scope.launch {
                val localAddresses: List<InetAddress> = NetworkInterface.getNetworkInterfaces()
                    .toList().flatMap { it.interfaceAddresses }.map { it.address }

                if(neighborHostAddr !in localAddresses) {
                    listener.onNeighborDiscovered(neighborHostAddr.hostName, serviceInfo.port)
                }else {
                    logger.e(DCACHE_LOGTAG, "Error: could not get neighborUrl. Url should not " +
                            "have been null after service resolved")
                }
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
            if(!service.serviceType.serviceTypeMatches(SERVICE_TYPE)) {
                logger.d(DCACHE_LOGTAG, "Unknown Service Type: ${service.serviceType}")
            }else {
                logger.d(DCACHE_LOGTAG, "Service Found: ${service.serviceType}")
                nsdManager.resolveService(service, ResolveListener())
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            logger.e(DCACHE_LOGTAG, "service lost: $service")
            val neighborIpAddr = service.host?.hostAddress
            if(neighborIpAddr != null) {
                listener.onNeighborLost(neighborIpAddr, service.port)
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
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }


    companion object {

        const val SERVICE_NAME = "DCache"

        const val SERVICE_TYPE = "_dcache._tcp"

    }


}