package com.ustadmobile.core.account

import org.kodein.di.bindings.Scope
import org.kodein.di.bindings.ScopeRegistry
import org.kodein.di.bindings.StandardScopeRegistry

class LearningSpaceScope: Scope<LearningSpace> {

    private val activeEndpoints = mutableMapOf<String, ScopeRegistry>()

    val activeEndpointUrls: Set<String>
        get() = activeEndpoints.keys

    override fun getRegistry(context: LearningSpace): ScopeRegistry = activeEndpoints.getOrPut(context.url) { StandardScopeRegistry() }

    companion object {

        val Default = LearningSpaceScope()

    }
}