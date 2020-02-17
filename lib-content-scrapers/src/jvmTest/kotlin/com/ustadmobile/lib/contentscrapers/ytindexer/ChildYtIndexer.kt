package com.ustadmobile.lib.contentscrapers.ytindexer

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.YoutubePlaylistIndexer

class ChildYtIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase) : YoutubePlaylistIndexer(parentContentEntry, runUid, db, 0)  {

    override fun indexUrl(sourceUrl: String) {
    }

    override fun close() {
    }


}