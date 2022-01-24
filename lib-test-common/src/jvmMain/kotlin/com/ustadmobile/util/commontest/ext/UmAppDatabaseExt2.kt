package com.ustadmobile.util.commontest.ext

import com.ustadmobile.core.db.UmAppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.io.PipedInputStream
import java.io.PipedOutputStream
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.util.ext.hexStringToBase64Encoded
import org.junit.Assert
import java.io.ByteArrayOutputStream
import java.io.File

fun UmAppDatabase.mockResponseForConcatenatedFiles2Request(request: RecordedRequest) : MockResponse{

    val headers = request.headers?.toMultimap() ?: mapOf()
    val bout = ByteArrayOutputStream()
    request.body.writeTo(bout)
    bout.flush()
    val md5s = String(bout.toByteArray())

    val md5List = md5s.split(";").map {
        it.hexStringToBase64Encoded()
    }

    val concatResponse = containerEntryFileDao.generateConcatenatedFilesResponse2(md5List,
            headers, this)

    val pipeOut = PipedOutputStream()
    val pipeIn = PipedInputStream(pipeOut)
    GlobalScope.launch {
        concatResponse.writeTo(pipeOut)
        pipeOut.close()
    }

    return MockResponse().setBody(Buffer().readFrom(pipeIn))
            .setResponseCode(concatResponse.status)
            .apply {
                concatResponse.rangeResponse?.responseHeaders?.forEach {
                    addHeader(it.key, it.value)
                }
            }

}

/**
 * Assert that one container and another contain exactly the same content. This will first
 * check that they container the same number of entries. Then it will check that each entry
 * present in the first container is present in the second container, and that the content bytes
 * for each entry are exactly the same.
 */
fun UmAppDatabase.assertContainerEqualToOther(containerUid: Long, otherDb: UmAppDatabase,
                                              acceptEmptyContainer: Boolean = false) {
    val entriesInThisDb = containerEntryDao.findByContainer(containerUid)
    val entriesInOtherDb = otherDb.containerEntryDao.findByContainer(containerUid)

    if(!acceptEmptyContainer) {
        Assert.assertNotEquals("Container is not empty", 0,
            entriesInThisDb.size)
    }

    Assert.assertEquals("Same number of entries in both containers", entriesInThisDb.size,
            entriesInOtherDb.size)

    entriesInThisDb.forEach {entryInThis ->
        val entryInOther = entriesInOtherDb.firstOrNull { it.cePath == entryInThis.cePath }
                ?: throw IllegalStateException("ContainerEntry ${entryInThis.cePath} not in other db")
        Assert.assertArrayEquals("Contents of ${entryInThis.cePath} are the same",
                File(entryInThis.containerEntryFile!!.cefPath!!).readBytes(),
                File(entryInOther.containerEntryFile!!.cefPath!!).readBytes())
    }
}