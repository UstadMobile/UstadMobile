package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.CommunicationManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UstadCommunicationManager(
        val workerLE: ExecutorService = Executors.newFixedThreadPool(10),
        val validatorLE: ExecutorService = Executors.newFixedThreadPool(4))
    : CommunicationManager(workerLE, validatorLE){

    override fun stop() {
        super.stop()
        workerLE.shutdown()
        validatorLE.shutdown()
    }

}