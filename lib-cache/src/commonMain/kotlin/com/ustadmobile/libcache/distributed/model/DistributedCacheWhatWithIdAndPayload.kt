package com.ustadmobile.libcache.distributed.model

import java.nio.ByteBuffer

interface DistributedCacheWhatWithIdAndPayload {

    val id: Long

    val payload: ByteArray

    companion object {

        //What byte, id, payloadLen
        const val OVERHEAD_SIZE = 1 + 8 + 2

    }
}

//Shared between ping and pong
internal fun DistributedCacheWhatWithIdAndPayload.toBytesArray(what: Byte) : ByteArray {
    val buffer = ByteBuffer.allocate(DistributedCacheWhatWithIdAndPayload.OVERHEAD_SIZE + payload.size)

    buffer.put(what)
    buffer.putLong(id)
    buffer.putShort(payload.size.toShort())
    buffer.put(payload)
    return buffer.array()
}

internal fun ByteBuffer.readIdAndPayload(): Pair<Long, ByteArray> {
    val id = long
    val payloadLen = short
    val payload = ByteArray(payloadLen.toInt())

    if(payloadLen > 0) {
        get(payload)
    }

    return Pair(id, payload)
}
