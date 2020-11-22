package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.NetworkNode

interface NetworkNodeListView : UstadView{

    var deviceName: String?

    var deviceList: DoorLiveData<List<NetworkNode>>?

    companion object {

        const val VIEW_NAME = "NetworkNodeList"
    }

}