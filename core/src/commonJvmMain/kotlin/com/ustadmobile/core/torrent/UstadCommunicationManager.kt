package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.CommunicationManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UstadCommunicationManager(
        val worker: CommunicationWorkers
) : CommunicationManager(worker.workerLE, worker.validatorLE)