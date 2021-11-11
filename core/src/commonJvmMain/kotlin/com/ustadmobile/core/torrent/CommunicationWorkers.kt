package com.ustadmobile.core.torrent

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CommunicationWorkers(val workerLE: ExecutorService = Executors.newFixedThreadPool(10),
                           val validatorLE: ExecutorService = Executors.newFixedThreadPool(4)
)