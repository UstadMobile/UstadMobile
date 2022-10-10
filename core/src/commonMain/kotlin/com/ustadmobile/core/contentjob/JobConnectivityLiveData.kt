package com.ustadmobile.core.contentjob

import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.DoorMediatorLiveData


/**
 *  LiveDataMediator that combines live data for the current connectivity
 *  and whether or not the job can run on a metered network.
 *  This LiveData can then be used as a source to make a decision
 *  about whether or not a job needs to stop.
 */
class JobConnectivityLiveData(
        val connectivityLiveData: ConnectivityLiveData,
        val meteredAllowedLiveData: LiveData<Boolean>
) : DoorMediatorLiveData<Pair<Int, Boolean>>() {

    var connectivityState: Int? = null

    var meteredConnectionAllowed: Boolean? = null

    init{
        addSource(connectivityLiveData.liveData){
            if(it == null || it.connectivityState == connectivityState){
                return@addSource
            }
            connectivityState = it.connectivityState
            val meteredAllowed = meteredConnectionAllowed ?: return@addSource

           setValue(Pair(it.connectivityState, meteredAllowed))
        }

        addSource(meteredAllowedLiveData){
            if(it == meteredConnectionAllowed){
                return@addSource
            }
            meteredConnectionAllowed = it
            val connectivityState = connectivityState ?: return@addSource
           setValue(Pair(connectivityState, it))
        }

    }
}