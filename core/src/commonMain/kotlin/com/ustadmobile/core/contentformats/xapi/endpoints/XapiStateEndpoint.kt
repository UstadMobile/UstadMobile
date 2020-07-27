package com.ustadmobile.core.contentformats.xapi.endpoints

import com.ustadmobile.core.contentformats.xapi.State

interface XapiStateEndpoint {

    fun storeState(state: State)

    fun overrideState(state: State)

    fun deleteStateContent(stateId: String, agentJson: String, activityId: String, registration: String)

    fun deleteListOfStates(agentJson: String, activityId: String, registration: String)

}