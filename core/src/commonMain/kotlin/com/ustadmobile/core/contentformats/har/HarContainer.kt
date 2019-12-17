package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.container.ContainerManager

class HarContainer(containerManager: ContainerManager) {

    lateinit var startingUrl: String

    init{

        val index = containerManager.getEntry("harcontent")
        var regexList = containerManager.getEntry("regexList")




    }


    fun serve(request: HarRequest): HarResponse {


        return HarResponse()
    }



}