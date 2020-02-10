package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.HarIndexer
import java.net.URL

class KhanTopicIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase) : HarIndexer(parentContentEntry, runUid, db) {


    override fun indexUrl(sourceUrl: String) {

        var path =  URL(sourceUrl).path

        

        val list = startHarIndexer(sourceUrl, listOf(Regex("/content$path"))){
            true
        }
    }


}