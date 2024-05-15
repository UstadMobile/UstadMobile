package com.ustadmobile.core.contentformats.xapi.endpoints

import com.ustadmobile.core.domain.xapi.model.State
import org.kodein.di.DIAware

interface XapiStateEndpoint : DIAware {

    fun storeState(state: State)

    fun overrideState(state: State)

    fun getContent(stateId: String, agentJson: String, activityId: String, registration: String, since: String): String

    fun deleteStateContent(stateId: String, agentJson: String, activityId: String, registration: String)

    fun deleteListOfStates(agentJson: String, activityId: String, registration: String)

}