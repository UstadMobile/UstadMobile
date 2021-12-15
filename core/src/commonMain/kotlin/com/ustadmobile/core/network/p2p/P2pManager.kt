package com.ustadmobile.core.network.p2p

interface P2pManager {

    //Check the queue when something is added to watchlist, when a new node is discovered, or
    //when an executor finishes.

    /**
     * When a presenter or other component (e.g. ContentJobRunner) wants to monitor the status of
     * a given cotnainer, it should call this function. Availability information will be stored
     * in the database and accessed as LiveData or RateLimitedLiveData
     */
    fun addToWatchList(contianerUids: List<Long>)

    fun registerService(port: Int)

}