package com.ustadmobile.libcache.distributed

import android.content.Context
import com.toxicbakery.library.nsd.rx.NsdManagerRx
import com.toxicbakery.library.nsd.rx.discovery.DiscoveryConfiguration
import com.toxicbakery.library.nsd.rx.discovery.DiscoveryEvent
import com.toxicbakery.library.nsd.rx.discovery.DiscoveryServiceFound
import com.toxicbakery.library.nsd.rx.discovery.DiscoveryServiceLost
import com.toxicbakery.library.nsd.rx.discovery.DiscoveryStarted
import com.toxicbakery.library.nsd.rx.discovery.DiscoveryStopped
import com.toxicbakery.library.nsd.rx.registration.RegistrationConfiguration
import com.toxicbakery.library.nsd.rx.registration.RegistrationEvent
import com.toxicbakery.library.nsd.rx.registration.ServiceRegistered
import com.toxicbakery.library.nsd.rx.resolve.ServiceResolved
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.logging.UstadCacheLogger
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.InetAddress
import java.net.NetworkInterface

class DistributedCacheNsdAndroid(
    context: Context,
    private val port: Int,
    private val logger: UstadCacheLogger,
    private val listener: DistributedCacheNeighborDiscoveryListener,
) {

    private val nsdManagerRx = NsdManagerRx(context)

    init {
        nsdManagerRx.registerService(
            RegistrationConfiguration(port = port, serviceName = SERVICE_NAME, serviceType = SERVICE_TYPE)
        )
        .subscribe { event: RegistrationEvent ->
            val registeredService = (event as? ServiceRegistered)?.nsdServiceInfo
            logger.i(DCACHE_LOGTAG, "Registered ${registeredService?.serviceName} on port ${registeredService?.port}")
        }

        nsdManagerRx.discoverServices(DiscoveryConfiguration(SERVICE_TYPE))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { event: DiscoveryEvent ->
                when(event) {
                    is DiscoveryStarted -> {
                        logger.i(DCACHE_LOGTAG, "Discovery started")
                    }

                    is DiscoveryStopped -> {
                        logger.i(DCACHE_LOGTAG, "Discovery stopped")
                    }

                    is DiscoveryServiceFound -> {
                        logger.i(DCACHE_LOGTAG, "Discovered: ${event.service.serviceName}")
                        nsdManagerRx.resolveService(event.service).subscribe { resolveEvt ->
                            resolveEvt as ServiceResolved
                            logger.i(DCACHE_LOGTAG, "Resolved: ${resolveEvt.nsdServiceInfo.serviceName}")
                            val localAddresses: List<InetAddress> = NetworkInterface.getNetworkInterfaces().toList()
                                .flatMap { it.interfaceAddresses }.map { it.address }
                            if(resolveEvt.nsdServiceInfo.host !in localAddresses) {
                                logger.i(DCACHE_LOGTAG, "Discovered neighbor: ${resolveEvt.nsdServiceInfo.serviceName}")
                                listener.onNeighborDiscovered(resolveEvt.nsdServiceInfo.host.hostName, resolveEvt.nsdServiceInfo.port)
                            }else {
                                logger.i(DCACHE_LOGTAG, "Same machine: ${resolveEvt.nsdServiceInfo.serviceName} ${resolveEvt.nsdServiceInfo.host.hostName}:${resolveEvt.nsdServiceInfo.port}")
                            }
                        }
                    }

                    is DiscoveryServiceLost -> {
                        logger.i(DCACHE_LOGTAG, "Lost: ${event.service.serviceName}")
                    }
                }
            }

    }


    companion object {

        const val SERVICE_NAME = "DCache"

        const val SERVICE_TYPE = "_dcache._tcp"

    }


}