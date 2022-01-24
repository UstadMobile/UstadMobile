package com.ustadmobile.core.network

/**
 * Simple functional interface for progress listening. The progress on the network task does not
 * match 1:1 with the progress on the job (which may have other tasks e.g. importing to complete,
 * and which may have resumed some progress etc).
 *
 * The progress reported here is the raw progress on the underlying http request.
 */
fun interface NetworkProgressListener {

    /**
     * Emitted periodically when progress completes on a download or upload task
     * @param bytesCompleted the total bytes completed including any bytes that were skipped using
     * a range request, not including any bytes that are not part of this request (e.g. entries
     * skipped because the remote side already had them)
     *
     * @param totalBytes the total bytes that are part of this http request (including anything that
     * might be skipped using a range request).
     */
    fun onProgress(bytesCompleted: Long, totalBytes: Long)

}