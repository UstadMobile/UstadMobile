package com.ustadmobile.core.account

import org.kodein.di.bindings.Scope
import org.kodein.di.bindings.ScopeRegistry
import org.kodein.di.bindings.StandardScopeRegistry

class EndpointScope: Scope<Endpoint> {

    private val activeEndpoints = mutableMapOf<String, ScopeRegistry>()

    val activeEndpointUrls: Set<String>
        get() = activeEndpoints.keys

    val activeEndpointSet: Set<Endpoint>
        get() = activeEndpoints.keys.map { Endpoint(it) }.toSet()

    override fun getRegistry(context: Endpoint): ScopeRegistry = activeEndpoints.getOrPut(context.url) { StandardScopeRegistry() }

    companion object {

        val Default = EndpointScope()

    }
}