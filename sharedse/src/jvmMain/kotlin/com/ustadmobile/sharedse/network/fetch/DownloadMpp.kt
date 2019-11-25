package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Status

actual interface DownloadMpp {
    /** Used to identify a download. This id also matches the id of the request that started
     * the download.*/
    actual val id: Int
    /** The Fetch namespace this download belongs to.*/
    actual val namespace: String
    /** The url where the file will be downloaded from.*/
    actual val url: String
    /** The file eg(/files/download.txt) where the file will be
     * downloaded to and saved on disk.*/
    actual val file: String
    /** The group id this download belongs to.*/
    actual val group: Int
    /** The headers used by the downloader to send header information to
     * the server about a request.*/
    actual val headers: Map<String, String>
    /** The amount of bytes downloaded thus far and saved to the file.*/
    actual val downloaded: Long
    /** The file size of a download in bytes. This field could return -1 if the server
     * did not readily provide the Content-Length when the connection was established.*/
    actual val total: Long
    /** The current status of a download.
     *  @see com.tonyodev.fetch2.Status
     *  */
    actual val status: Status
    /** If the download encountered an error, the download status will be Status.Failed and
     *  this field will provide the specific error when possible.
     *  Otherwise the default non-error value is Error.NONE.
     *  @see com.tonyodev.fetch2.Error
     *  */
    actual val error: Error

}