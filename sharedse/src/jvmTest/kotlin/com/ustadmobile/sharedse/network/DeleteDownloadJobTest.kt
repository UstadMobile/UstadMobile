package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeleteDownloadJobTest{

    private lateinit var rootContentEntry: ContentEntry
    private lateinit var standAloneEntry: ContentEntry

    private lateinit var containerEntryFileZombie: ContainerEntryFile
    private lateinit var containerEntryFileStandAlone: ContainerEntryFile
    private lateinit var containerEntryFileXyz: ContainerEntryFile
    private lateinit var containerEntryFileAbc: ContainerEntryFile



    lateinit var db: UmAppDatabase

    @Before
    fun setup(){

        db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()

        var entryFileDao = db.containerEntryFileDao
        var containerEntryDao = db.containerEntryDao
        var containerDao = db.containerDao
        var entryDao = db.contentEntryDao
        var dwDao = db.downloadJobItemDao
        var dwJoinDao = db.downloadJobItemParentChildJoinDao
        var statusDao = db.contentEntryStatusDao


        containerEntryFileAbc = ContainerEntryFile()
        containerEntryFileAbc.cefUid = 1
        containerEntryFileAbc.cefPath = "somewhere/abc"
        entryFileDao.insert(containerEntryFileAbc)


        containerEntryFileXyz = ContainerEntryFile()
        containerEntryFileXyz.cefUid = 2
        containerEntryFileXyz.cefPath = "somewhere/xyz"
        entryFileDao.insert(containerEntryFileXyz)

        containerEntryFileStandAlone = ContainerEntryFile()
        containerEntryFileStandAlone.cefUid = 4
        containerEntryFileStandAlone.cefPath = "standalone"
        entryFileDao.insert(containerEntryFileStandAlone)

        containerEntryFileZombie = ContainerEntryFile()
        containerEntryFileZombie.cefUid = 3
        containerEntryFileZombie.cefPath = "futurezombie"
        entryFileDao.insert(containerEntryFileZombie)


        // standalone child - should not be deleted by test, has a file called abc
        var standaloneChild = DownloadJobItem()
        standaloneChild.djiUid = 3
        standaloneChild.timeStarted = 46366
        standaloneChild.djiContainerUid = 7
        standaloneChild.djiContentEntryUid = 3
        dwDao.insert(standaloneChild)

        standAloneEntry = ContentEntry()
        standAloneEntry.contentEntryUid = 3
        entryDao.insert(standAloneEntry)

        var containerOfStandAlone = Container()
        containerOfStandAlone.containerUid = standaloneChild.djiContainerUid
        containerOfStandAlone.containerContentEntryUid = standAloneEntry.contentEntryUid
        containerDao.insert(containerOfStandAlone)

        var standaloneContainerEntry = ContainerEntry()
        standaloneContainerEntry.ceContainerUid = containerOfStandAlone.containerUid
        standaloneContainerEntry.cePath = "java"
        standaloneContainerEntry.ceCefUid = containerEntryFileStandAlone.cefUid
        standaloneContainerEntry.ceUid = 1
        containerEntryDao.insert(standaloneContainerEntry)

        var standaloneContainerEntrytwo = ContainerEntry()
        standaloneContainerEntrytwo.ceContainerUid = containerOfStandAlone.containerUid
        standaloneContainerEntrytwo.cePath = "here/abc"
        standaloneContainerEntrytwo.ceCefUid = containerEntryFileAbc.cefUid
        standaloneContainerEntrytwo.ceUid = 2
        containerEntryDao.insert(standaloneContainerEntrytwo)

        var standAloneStatus = ContentEntryStatus()
        standAloneStatus.cesUid = standAloneEntry.contentEntryUid
        statusDao.insert(standAloneStatus)


        // end standalone

        // start root

        var dwroot = DownloadJobItem()
        dwroot.djiUid = 1
        dwroot.timeStarted = 242353456
        dwroot.djiContentEntryUid = 1
        dwDao.insert(dwroot)

        rootContentEntry = ContentEntry()
        rootContentEntry.contentEntryUid = 1
        entryDao.insert(rootContentEntry)

        var rootStatus = ContentEntryStatus()
        rootStatus.cesUid = rootContentEntry.contentEntryUid
        statusDao.insert(rootStatus)

        var dwparent = DownloadJobItem()
        dwparent.djiUid = 2
        dwparent.timeStarted = 54446
        dwparent.djiContentEntryUid = 2
        dwDao.insert(dwparent)

        var parentEntry = ContentEntry()
        parentEntry.contentEntryUid = 2
        entryDao.insert(parentEntry)

        var parentEntryStatus = ContentEntryStatus()
        parentEntryStatus.cesUid = parentEntry.contentEntryUid
        statusDao.insert(parentEntryStatus)

        var rootParentJoin = DownloadJobItemParentChildJoin()
        rootParentJoin.djiParentDjiUid = dwroot.djiUid
        rootParentJoin.djiChildDjiUid =  dwparent.djiUid
        rootParentJoin.djiPcjUid = 1
        dwJoinDao.insert(rootParentJoin)

        var dwchildofparent = DownloadJobItem()
        dwchildofparent.djiUid = 5
        dwchildofparent.timeStarted = 54545
        dwchildofparent.djiContentEntryUid = 5
        dwchildofparent.djiContainerUid = 8
        dwDao.insert(dwchildofparent)

        var childOfParent = ContentEntry()
        childOfParent.contentEntryUid = 5
        entryDao.insert(childOfParent)

        var childOfparentStatus = ContentEntryStatus()
        childOfparentStatus.cesUid = childOfParent.contentEntryUid
        statusDao.insert(childOfparentStatus)

        var childOfParentJoin = DownloadJobItemParentChildJoin()
        childOfParentJoin.djiParentDjiUid = dwparent.djiUid
        childOfParentJoin.djiChildDjiUid = dwchildofparent.djiUid
        childOfParentJoin.djiPcjUid = 3
        dwJoinDao.insert(childOfParentJoin)

        var containerchildofparent = Container()
        containerchildofparent.containerUid = 8
        containerchildofparent.containerContentEntryUid = childOfParent.contentEntryUid
        containerDao.insert(containerchildofparent)

        var containerEntrychild = ContainerEntry()
        containerEntrychild.ceContainerUid = containerchildofparent.containerUid
        containerEntrychild.cePath = "content/abc.html"
        containerEntrychild.ceCefUid = containerEntryFileAbc.cefUid
        containerEntrychild.ceUid = 3
        containerEntryDao.insert(containerEntrychild)

        var containerEntrychildtwo = ContainerEntry()
        containerEntrychildtwo.ceContainerUid = containerchildofparent.containerUid
        containerEntrychildtwo.cePath = "content/xyz.html"
        containerEntrychildtwo.ceCefUid = containerEntryFileXyz.cefUid
        containerEntrychildtwo.ceUid = 4
        containerEntryDao.insert(containerEntrychildtwo)

        var dwparenttwo = DownloadJobItem()
        dwparenttwo.djiUid = 4
        dwparenttwo.timeStarted = 453534
        dwparenttwo.djiContainerUid = 9
        dwparenttwo.djiContentEntryUid = 4
        dwDao.insert(dwparenttwo)

        var parentTwoEntry = ContentEntry()
        parentTwoEntry.contentEntryUid = 4
        entryDao.insert(parentTwoEntry)

        var parentTwoStatus = ContentEntryStatus()
        parentTwoStatus.cesUid = parentTwoEntry.contentEntryUid
        statusDao.insert(parentTwoStatus)

        var rootParentTwoJoin = DownloadJobItemParentChildJoin()
        rootParentTwoJoin.djiParentDjiUid = dwroot.djiUid
        rootParentTwoJoin.djiChildDjiUid = dwparenttwo.djiUid
        rootParentTwoJoin.djiPcjUid = 2
        dwJoinDao.insert(rootParentTwoJoin)

        var containerwchildofparentwo = Container()
        containerwchildofparentwo.containerUid = 9
        containerwchildofparentwo.containerContentEntryUid = parentTwoEntry.contentEntryUid
        containerDao.insert(containerwchildofparentwo)

        var parentTwoContainerEntry = ContainerEntry()
        parentTwoContainerEntry.ceContainerUid = containerwchildofparentwo.containerUid
        parentTwoContainerEntry.cePath = "othercontent/zombie.html"
        parentTwoContainerEntry.ceCefUid = containerEntryFileAbc.cefUid
        containerEntryDao.insert(parentTwoContainerEntry)

        var containerEntrychildthree = ContainerEntry()
        containerEntrychildthree.ceContainerUid = containerwchildofparentwo.containerUid
        containerEntrychildthree.cePath = "zombie.css"
        containerEntrychildthree.ceCefUid = containerEntryFileZombie.cefUid
        containerEntryDao.insert(containerEntrychildthree)


    }

    @Test
    fun givenRootEntry_whenDeleted_checkAllChildrenDeleted(){
        deleteDownloadJob(db, rootContentEntry.contentEntryUid) {
            println(it)
        }
        Assert.assertNotNull(db.containerEntryFileDao.findByUid(containerEntryFileAbc.cefUid))
        Assert.assertNotNull(db.containerEntryFileDao.findByUid(containerEntryFileStandAlone.cefUid))

        Assert.assertNull(db.containerEntryFileDao.findByUid(containerEntryFileZombie.cefUid))
        Assert.assertNull(db.containerEntryFileDao.findByUid(containerEntryFileXyz.cefUid))


    }

    @Test
    fun givenSingleEntry_whenDeleted_checkDeleted(){
        deleteDownloadJob(db, standAloneEntry.contentEntryUid) {
            println(it)
        }

        Assert.assertNull(db.containerEntryFileDao.findByUid(containerEntryFileStandAlone.cefUid))

        Assert.assertNotNull(db.containerEntryFileDao.findByUid(containerEntryFileAbc.cefUid))
        Assert.assertNotNull(db.containerEntryFileDao.findByUid(containerEntryFileZombie.cefUid))
        Assert.assertNotNull(db.containerEntryFileDao.findByUid(containerEntryFileXyz.cefUid))
    }


}