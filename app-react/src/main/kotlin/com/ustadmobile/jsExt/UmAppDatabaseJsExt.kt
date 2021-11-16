package com.ustadmobile.jsExt

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.lib.util.randomString
import kotlinx.browser.localStorage

suspend fun UmAppDatabase.insertContentEntryWithParentChildJoinAndMostRecentContainer(
    numEntries: Int, parentEntryUid: Long, nonLeafIndexes: MutableList<Int> = mutableListOf()
): List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {
    return (1 .. numEntries).map {
        val entry = ContentEntry().apply {
            leaf = !(nonLeafIndexes.isNotEmpty() && nonLeafIndexes.indexOf(it - 1) != -1)
            title = "Dummy ${if(leaf) " entry" else "folder"} title $it"
            description = "Dummy description $it"
            contentEntryUid = contentEntryDao.insertAsync(this)
        }
        val parentChildJoin = ContentEntryParentChildJoin().apply {
            cepcjChildContentEntryUid = entry.contentEntryUid
            cepcjParentContentEntryUid = parentEntryUid
            cepcjUid = contentEntryParentChildJoinDao.insertAsync(this)
        }

        val container = Container().apply {
            fileSize = 10000
            cntLastModified = getSystemTimeInMillis()
            containerContentEntryUid = entry.contentEntryUid
            containerUid = containerDao.insertAsync(this)
        }



        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
            mostRecentContainer = container
            contentEntryParentChildJoin = parentChildJoin
            contentEntryUid = entry.contentEntryUid
            leaf = entry.leaf
            title = entry.title
            description = entry.description
            contentEntryUid = entry.contentEntryUid
        }
    }
}

suspend fun UmAppDatabase.initJsRepo(preloaded: Boolean){

    if(!preloaded){
        localStorage.clear()
        roleDao.insertDefaultRolesIfRequired()
    }

    val site = siteDao.getSiteAsync()
    if(site == null){
        siteDao.insertAsync(Site().apply {
            siteUid = 1L
            siteName = "My Site"
            guestLogin = false
            registrationAllowed = false
            authSalt = randomString(20)
        })
    }
}