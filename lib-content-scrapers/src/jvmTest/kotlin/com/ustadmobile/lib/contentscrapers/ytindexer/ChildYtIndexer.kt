package com.ustadmobile.lib.contentscrapers.ytindexer

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.abztract.YoutubePlaylistIndexer
import org.kodein.di.DI

@ExperimentalStdlibApi
class ChildYtIndexer(parentContentEntry: Long, runUid: Int, endpoint: Endpoint, di: DI) : YoutubePlaylistIndexer(parentContentEntry, runUid, 0, 0, endpoint, di) {

    override fun indexUrl(sourceUrl: String) {
    }

    override fun close() {
    }


}