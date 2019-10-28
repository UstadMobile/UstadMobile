package com.ustadmobile.sharedse.util

import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.sharedse.network.BleEntryStatusTask
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_COMPLETED
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_FAILURE
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_NONE
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_QUEUED
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_RUNNING
import com.ustadmobile.sharedse.network.BleMessage
import com.ustadmobile.sharedse.network.BleMessageResponseListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Class which handle the execution of the BleEntryStatusTask
 *
 * @author kileha3
 */

class EntryTaskExecutor(numProcessors: Int = 1, private val scope: CoroutineScope = GlobalScope)
    :BleMessageResponseListener{


    private val mTaskChannel: Channel<BleEntryStatusTask> = Channel(numProcessors)

    val taskQueue = copyOnWriteListOf<BleEntryStatusTask>()

    val runningOrCompletedTasks = copyOnWriteListOf<BleEntryStatusTask>()

    private lateinit var mCoroutineCtx: CoroutineContext


    private suspend fun start(taskChannel: ReceiveChannel<BleEntryStatusTask>){
        for(task in taskChannel){
            task.sendRequest()
            runningOrCompletedTasks[runningOrCompletedTasks.indexOf(task)].status = STATUS_RUNNING
        }
    }


    suspend fun execute(task: BleEntryStatusTask){
        this.mCoroutineCtx = coroutineContext
        task.responseListener = this
        taskQueue.add(task)
        submitTasks()
        scope.launch {
            launch {
                start(mTaskChannel)
            }
        }
    }


    override fun onResponseReceived(sourceDeviceAddress: String, response: BleMessage?, error: Exception?) {
        val taskToRemove = getTaskByNodeAddress(sourceDeviceAddress)
        val index = runningOrCompletedTasks.indexOf(taskToRemove)
        if (taskToRemove != null){
            taskQueue.remove(taskToRemove)
            runningOrCompletedTasks[index].status = if(error == null) STATUS_COMPLETED else STATUS_FAILURE
            scope.launch {
                submitTasks()
            }
        }
    }

    private suspend fun submitTasks(){
        val tasksToExecute = taskQueue
                .filter { queuedTask -> (!runningOrCompletedTasks.contains(queuedTask)
                        && queuedTask.status == STATUS_NONE)}
        for(queuedTask in tasksToExecute){
            runningOrCompletedTasks.add(queuedTask)
            queuedTask.status = STATUS_QUEUED
            mTaskChannel.send(queuedTask)
        }

    }

    private fun getTaskByNodeAddress(nodeAddress: String): BleEntryStatusTask?{
        taskQueue.forEach {
            if(it.networkNode.bluetoothMacAddress == nodeAddress){
                return it
            }
        }
        return null
    }

    fun stop(){
        if(::mCoroutineCtx.isInitialized){
            mCoroutineCtx.cancel()
        }
    }
}