package com.ustadmobile.sharedse.network.containerfetcher

import com.ustadmobile.sharedse.network.NetworkManagerBle


actual class ContainerFetcherBuilder actual constructor(networkManager: NetworkManagerBle){

    private val networkManagerVal = networkManager

    actual fun build(): ContainerFetcher {
        return ContainerFetcherJvm(networkManagerVal)
    }

}