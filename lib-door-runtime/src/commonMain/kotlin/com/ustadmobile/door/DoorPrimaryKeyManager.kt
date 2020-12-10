package com.ustadmobile.door

import com.ustadmobile.door.util.generateDoorNodeId
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.door.util.threadSafeMapOf
import kotlinx.atomicfu.atomic
import kotlin.math.pow

/**
 * Manage generation of unique primary keys for each table. This is inspired by the Twitter snowflake
 * approach. It is slightly modified so that the 64bit keys are generated as follows:
 *
 * 32 bits: unix timestamp (offset by CUSTOM_EPOCH)
 * 19 bits: Node Id
 * 12 bits: Sequence number
 * 1 bit (sign bit): unused
 *
 * This allows 542,288 nodes to create 4,096 unique new entries per second (per table). This supports
 * more unique nodes than snowflake with fewer unique keys per second. This seems appropriate as most
 * work is delegated to the client.
 */
class DoorPrimaryKeyManager() {

    private val tableKeyManagers = threadSafeMapOf<Int, TablePrimaryKeyManager>()

    private inline val timestamp: Long
        get() = (systemTimeInMillis() / 1000) - CUSTOM_EPOCH

    val nodeId: Int by lazy(LazyThreadSafetyMode.NONE) {
        generateDoorNodeId(MAX_NODE_ID)
    }

    inner class TablePrimaryKeyManager() {

        val seqNum = atomic(0)

        val lastTimestamp = atomic(0L)

        fun nextId(): Long {
            var sequenceNum = seqNum.getAndIncrement()

            val currentTimestamp = timestamp

            val lastTimestampVal = lastTimestamp.getAndSet(currentTimestamp)


            if(sequenceNum == MAX_SEQUENCE) {
                if(currentTimestamp > lastTimestampVal) {
                    seqNum.value = 1
                    sequenceNum = 0
                }else {
                    //we need to wait here
                    //TODO: Put expect/actual here to handle this scenario
                    throw IllegalStateException("Out of primary keys for a second...")
                }

            }

            return currentTimestamp shl (NODE_ID_BITS + SEQUENCE_BITS) or
                    (nodeId.toLong() shl SEQUENCE_BITS) or
                    sequenceNum.toLong()
        }

    }

    fun nextId(tableId: Int): Long {
        return tableKeyManagers.getOrPut(tableId) {
            TablePrimaryKeyManager()
        }.nextId()
    }

    companion object {
        const val UNUSED_BITS = 1

        const val EPOCH_BITS = 32

        const val NODE_ID_BITS = 19

        const val SEQUENCE_BITS = 12

        val MAX_NODE_ID = 2f.pow(NODE_ID_BITS).toInt()

        val MAX_SEQUENCE = 2f.pow(SEQUENCE_BITS).toInt()

        // Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)

        //01/Jan/2020 at 00:00:00 UTC
        const val CUSTOM_EPOCH = 1577836800
    }

}
