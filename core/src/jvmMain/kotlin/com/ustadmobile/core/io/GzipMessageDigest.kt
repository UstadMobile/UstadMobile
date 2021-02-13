package com.ustadmobile.core.io

import com.ustadmobile.door.util.NullOutputStream
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

/**
 * To verify data integrity ConcatenatedInput and ConcatenatedOutput streams need to md5 sum,
 */
class GzipMessageDigest(val messageDigest: MessageDigest) {

    val buf = ByteArray(8 * 1024)

    @Volatile
    lateinit var pipeOut: PipedOutputStream

    @Volatile
    lateinit var pipeIn: PipedInputStream

    @Volatile
    private var inflate: Boolean = false

    @Volatile
    private var readJob: Job? = null

    fun reset(inflateEnabled: Boolean) {
        pipeOut = PipedOutputStream()
        pipeIn = PipedInputStream(pipeOut)

        inflate = inflateEnabled
        messageDigest.reset()
    }

    fun update(byteArray: ByteArray, offset: Int = 0, len: Int = byteArray.size) {
        val inflateEnabled = inflate
        if(readJob == null) {
            readJob = GlobalScope.launch(Dispatchers.IO) {
                val digestInput = if(inflateEnabled) {
                    GZIPInputStream(pipeIn)
                }else {
                    pipeIn
                }

                DigestInputStream(digestInput, messageDigest).use {
                    val bytesRead = it.copyTo(NullOutputStream())
                    println("InflateMessageDigest: Read $bytesRead")
                }
            }
        }

        pipeOut.write(byteArray, offset, len)
    }

    fun digest() : ByteArray{
        pipeOut.flush()
        pipeOut.close()
        runBlocking { readJob?.join() }
        pipeIn.close()

        readJob = null

        return messageDigest.digest()
    }



}