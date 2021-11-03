package com.ustadmobile.core.contentjob

import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMediatorLiveData

class JobConnectivityLiveData(
        val connectivityLiveData: ConnectivityLiveData,
        val meteredAllowedLiveData: DoorLiveData<Boolean>)
    : DoorMediatorLiveData<Pair<Int, Boolean>>() {

    var connectivityState: Int? = null

    var meteredConnectionAllowed: Boolean? = null

    init{
        addSource(connectivityLiveData.liveData){
            if(it == null || it.connectivityState == connectivityState){
                return@addSource
            }
            connectivityState = it.connectivityState
            val meteredAllowed = meteredConnectionAllowed ?: return@addSource

            setVal(Pair(it.connectivityState, meteredAllowed))
        }

        addSource(meteredAllowedLiveData){
            if(it == meteredConnectionAllowed){
                return@addSource
            }
            meteredConnectionAllowed = it
            val connectivityState = connectivityState ?: return@addSource
            setVal(Pair(connectivityState, it))
        }

    }
}