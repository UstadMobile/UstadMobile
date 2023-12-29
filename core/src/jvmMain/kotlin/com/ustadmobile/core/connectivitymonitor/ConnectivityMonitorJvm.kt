package com.ustadmobile.core.connectivitymonitor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.Socket

/**
 * Simple connectivity monitor for JVM. Used to control Quartz jobs similar to WorkManager connectivity
 * constraint.
 */
class ConnectivityMonitorJvm(
    private val checkInetAddr: () -> InetAddress,
    private val checkPort: Int,
    private val interval: Int = 5_000,
) {

    enum class ConnectivityStatus {
        UNKNOWN, CONNECTED, DISCONNECTED
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _state = MutableStateFlow(ConnectivityStatus.UNKNOWN)

    val state: Flow<ConnectivityStatus> = _state.asStateFlow()

    @Suppress("unused")
    private val currentStatus: ConnectivityStatus = _state.value

    init {
        scope.launch {
            var currentJob: Job? = null
            while(isActive) {
                currentJob?.cancel()
                currentJob = scope.launch {
                    try {
                        Socket(checkInetAddr(), checkPort).close()
                        _state.takeIf {
                            it.value != ConnectivityStatus.CONNECTED
                        }?.value = ConnectivityStatus.CONNECTED
                    }catch(e: Throwable) {
                        //Will also catch cancellation exception caused by timeout
                        _state.takeIf {
                            it.value != ConnectivityStatus.DISCONNECTED
                        }?.value = ConnectivityStatus.DISCONNECTED
                    }
                }
                delay(interval.toLong())
            }
        }
    }

    fun close() {
        scope.cancel()
    }


}