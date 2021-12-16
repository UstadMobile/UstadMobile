package com.ustadmobile.core.contentjob

import kotlinx.coroutines.CancellationException

class ConnectivityCancellationException(val connectivityMessage: String): CancellationException(connectivityMessage) {

}