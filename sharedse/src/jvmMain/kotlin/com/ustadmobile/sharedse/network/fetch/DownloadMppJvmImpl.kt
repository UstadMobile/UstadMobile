package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Status
import okhttp3.Call

internal class DownloadMppJvmImpl(override val id: Int,
                                  override var namespace: String = "",
                                  override var url: String = "",
                                  override var file: String = "",
                                  override var group: Int = 0,
                                  override var headers: Map<String, String> = mutableMapOf(),
                                  override var downloaded: Long = 0,
                                  override var total: Long = 0,
                                  override var status: Status = Status.NONE,
                                  override var error: Error = Error.NONE) : DownloadMpp{
    var okHttpCall: Call? = null
}