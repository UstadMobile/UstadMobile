package com.ustadmobile.core.connectivitymonitor

import app.cash.turbine.test
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ConnectivityMonitorJvmTest {

    private lateinit var scope: CoroutineScope

    @BeforeTest
    fun setup() {
        scope = CoroutineScope(Dispatchers.Default + Job())
    }

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun givenSocketAvailable_whenInitialized_thenWillEmitConnected() {
        val serverSocket = ServerSocket(0)
        scope.launch {
            serverSocket.accept()
        }
        val connectivityMonitor = ConnectivityMonitorJvm(
            checkInetAddr = { InetAddress.getByName("localhost")},
            checkPort = serverSocket.localPort,
        )
        runBlocking {
            connectivityMonitor.state.filter {
                it == ConnectivityMonitorJvm.ConnectivityStatus.CONNECTED
            }.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
        }
        connectivityMonitor.close()
    }

    @Test
    fun givenSocketNotAvailable_whenInitialized_thenWillEmitDisconnected() {
        val serverSocket = ServerSocket(0)
        val port = serverSocket.localPort
        serverSocket.close()

        val connectivityMonitor = ConnectivityMonitorJvm(
            checkInetAddr = { InetAddress.getByName("localhost")},
            checkPort = port,
        )
        runBlocking {
            connectivityMonitor.state.filter {
                it == ConnectivityMonitorJvm.ConnectivityStatus.DISCONNECTED
            }.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
        }
        connectivityMonitor.close()
    }

    @Test
    fun givenSocketAVailableThenNot_whenInitialized_thenWillEmitConnectedThenDisconnected() {
        val serverSocket = ServerSocket(0)
        scope.launch {
            serverSocket.accept()
        }

        val connectivityMonitor = ConnectivityMonitorJvm(
            checkInetAddr = { InetAddress.getByName("localhost")},
            checkPort = serverSocket.localPort,
            interval = 300
        )

        runBlocking {
            connectivityMonitor.state.assertItemReceived {
                it == ConnectivityMonitorJvm.ConnectivityStatus.CONNECTED
            }
            serverSocket.close()
            connectivityMonitor.state.assertItemReceived(timeout = 5.seconds) {
                it == ConnectivityMonitorJvm.ConnectivityStatus.DISCONNECTED
            }
        }



    }

}