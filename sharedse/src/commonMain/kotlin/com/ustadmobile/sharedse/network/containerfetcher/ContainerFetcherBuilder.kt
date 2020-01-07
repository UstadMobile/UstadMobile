package com.ustadmobile.sharedse.network.containerfetcher

import com.ustadmobile.sharedse.network.NetworkManagerBle

expect class ContainerFetcherBuilder(networkManager: NetworkManagerBle) {

    fun build(): ContainerFetcher

}