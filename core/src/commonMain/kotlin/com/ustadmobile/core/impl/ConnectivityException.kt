package com.ustadmobile.core.impl

import kotlinx.coroutines.CancellationException

class ConnectivityException(val connectivityMessage: String): CancellationException(connectivityMessage) {

}