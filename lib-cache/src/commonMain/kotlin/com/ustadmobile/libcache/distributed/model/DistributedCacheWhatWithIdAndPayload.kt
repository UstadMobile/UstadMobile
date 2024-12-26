package com.ustadmobile.libcache.distributed.model

import java.nio.ByteBuffer

interface DistributedCacheWhatWithIdAndPayload {

    val id: Int

    val payload: ByteArray

    companion object {

        //What byte, id, payloadLen
        const val OVERHEAD_SIZE = 1 + 4 + 2

    }
}

//Shared between ping and pong
internal fun DistributedCacheWhatWithIdAndPayload.toBytesArray(what: Byte) : ByteArray {
    val buffer = ByteBuffer.allocate(DistributedCacheWhatWithIdAndPayload.OVERHEAD_SIZE + payload.size)

    buffer.put(what)
    buffer.putInt(id)
    buffer.putShort(payload.size.toShort())
    buffer.put(payload)
    return buffer.array()
}

internal fun ByteBuffer.readIdAndPayload(): Pair<Int, ByteArray> {
    val id = int
    val payloadLen = short
    val payload = ByteArray(payloadLen.toInt())

    if(payloadLen > 0) {
        get(payload)
    }

    return Pair(id, payload)
}
