package com.ustadmobile.libcache.distributed

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.logging.UstadCacheLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.withLock

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
 *
 * The service is generally registered whenever the application is running (e.g. created). Discovery
 * should generally run only when the application is in the foreground (e.g. started) because the
 * discovery process is an 'expensive' (e.g. power consuming) operation as per
 * https://developer.android.com/develop/connectivity/wifi/use-nsd#teardown .
 */
class DistributedCacheNsdAndroid(
    context: Context,
    private val port: Int,
    private val logger: UstadCacheLogger,
    private val listener: DistributedCacheNeighborDiscoveryListener,
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val serviceInfo = NsdServiceInfo().apply {
        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceName = SERVICE_NAME
        serviceType = SERVICE_TYPE
        setPort(this@DistributedCacheNsdAndroid.port)
    }

    inner class RegistrationListener : NsdManager.RegistrationListener {

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            logger.i(DCACHE_LOGTAG, "Registered: $serviceInfo")
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            logger.i(DCACHE_LOGTAG, "Unregistered: $serviceInfo")
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
                    listener.onNeighborDiscovered(
                        neighborIp = neighborHostAddr.hostName,
                        neighborUdpPort = serviceInfo.port,
                        neighborHttpPort = 0
                    )
                }else {
                    logger.d(DCACHE_LOGTAG, "$neighborHostAddr is local device")
                }
            }
        }
    }

    // Instantiate a new DiscoveryListener
    inner class DiscoveryListener : NsdManager.DiscoveryListener {

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
        private fun String.serviceTypeMatches(other: String): Boolean {
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

    @Volatile
    private var mDiscoveryListener: DiscoveryListener? = null

    private var mRegistrationListener: RegistrationListener? = null

    private val discoveryLock = ReentrantLock()

    private val registrationLock = ReentrantLock()

    private val lifecycleObserver = object: DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            registerService()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            unregisterService()
        }

        override fun onStart(owner: LifecycleOwner) {
            startDiscovery()
        }

        override fun onStop(owner: LifecycleOwner) {
            stopDiscovery()
        }
    }

    /**
     * Initialize the distributed cache to follow a LifecycleOwner . When the lifecycle state is at
     * least created, then the NSD service will be registered. When the lifecycle state is at least
     * started NSD discovery will run.
     */
    fun initWithLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        val currentState = lifecycleOwner.lifecycle.currentState
        if(currentState.isAtLeast(Lifecycle.State.CREATED))
            registerService()

        if(currentState.isAtLeast(Lifecycle.State.STARTED))
            startDiscovery()

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    /**
     * As per the docs: Service discovery is a more 'expensive' (e.g. battery consuming) operation.
     * This is best done only when the app is in the foreground (e.g. started by Activity.onStart)
     */
    fun startDiscovery() {
        discoveryLock.withLock {
            if(mDiscoveryListener == null) {
                mDiscoveryListener = DiscoveryListener().also {
                    nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, it)
                }
            }
        }
    }

    /**
     * Stop discovery: should generally be done when the app is going into the background.
     */
    fun stopDiscovery() {
        discoveryLock.withLock {
            val discoveryListenerVal = mDiscoveryListener
            if(discoveryListenerVal != null) {
                nsdManager.stopServiceDiscovery(discoveryListenerVal)
                mDiscoveryListener = null
            }
        }
    }

    fun registerService() {
        registrationLock.withLock {
            if(mRegistrationListener == null) {
                mRegistrationListener = RegistrationListener().also {
                    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, it)
                }
            }
        }
    }

    fun unregisterService() {
        registrationLock.withLock {
            val registrationListenerVal = mRegistrationListener
            if(registrationListenerVal != null) {
                nsdManager.unregisterService(registrationListenerVal)
                mRegistrationListener = null
            }
        }
    }

    companion object {

        const val SERVICE_NAME = "DCache"

        const val SERVICE_TYPE = "_dcache._tcp"

    }


}