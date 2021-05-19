package com.ustadmobile.core.io

import com.ustadmobile.door.util.NullOutputStream
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.io.IOException
import io.github.aakira.napier.Napier

/**
 * To verify data integrity ConcatenatedInput and ConcatenatedOutput streams need to md5 sum,
 */
class GzipMessageDigest(val messageDigest: MessageDigest) {

    val buf = ByteArray(8 * 1024)

    @Volatile
    private lateinit var pipeOut: PipedOutputStream

    @Volatile
    private lateinit var pipeIn: PipedInputStream

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
                    try {
                        it.copyTo(NullOutputStream())
                    }catch(e: IOException) {
                        // If there was an exception in this async job reading using pipeIn,
                        // then we need to close pipeOut to avoid a potential deadlock
                        // (where calling pipeOut.write blocks forever because there is nothing
                        // left to read from pipeIn).

                        Napier.e("GzipMessageDigest: Exception reading bytes for digest", e)
                        pipeOut.close()
                    }
                }
            }
        }

        try {
            pipeOut.write(byteArray, offset, len)
        }catch(e: IOException) {
            throw ConcatenatedDataIntegrityException("GzipMessageDigest: exception writing to digest " +
                    "pipe (data is probably corrupted)", e)
        }
    }

    fun digest() : ByteArray{
        pipeOut.flush()
        pipeOut.close()
        runBlocking { readJob?.join() }
        //pipeIn will be closed by the .use block in the coroutine job

        readJob = null

        return messageDigest.digest()
    }



}