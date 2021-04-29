package com.ustadmobile.core.networkmanager

import com.google.gson.Gson

private val defaultGson: Gson by lazy {Gson()}

@Deprecated("Should use DI, not this!")
fun defaultGson() = defaultGson


