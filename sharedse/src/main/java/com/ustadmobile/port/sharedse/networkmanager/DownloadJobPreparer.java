package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.DownloadJobItemManager;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin;
import com.ustadmobile.lib.util.UMUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This runnable sets up a download job so it's ready to run. It starts from a root content entry uid,
 * and then adds all
 */
public class DownloadJobPreparer implements Runnable {

    private DownloadJobItemManager jobItemManager;

    private UmAppDatabase appDatabase;

    private UmAppDatabase appDatabaseRepo;

    public DownloadJobPreparer(DownloadJobItemManager jobItemManager, UmAppDatabase appDatabase,
                               UmAppDatabase appDatabaseRepo) {
        this.jobItemManager = jobItemManager;
        this.appDatabase = appDatabase;
        this.appDatabaseRepo = appDatabaseRepo;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        int downloadJobUid = jobItemManager.getDownloadJobUid();
        long contentEntryUid = jobItemManager.getRootContentEntryUid();

        UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "DownloadJobPreparer: start " +
                "entry uid = " + contentEntryUid + " download job uid = " + downloadJobUid);

        int numItemsCreated = 0;

        DownloadJobItemDao jobItemDao = appDatabase.getDownloadJobItemDao();
        List<DownloadJobItemDao.DownloadJobItemToBeCreated2> childItemsToCreate;
        Container rootEntryContainer = appDatabaseRepo.getContainerDao()
                .getMostRecentContainerForContentEntry(contentEntryUid);
        DownloadJobItem rootDownlaodJobItem = new DownloadJobItem(
                jobItemManager.getDownloadJobUid(), contentEntryUid,
                rootEntryContainer != null ? rootEntryContainer.getContainerUid() : 0,
                rootEntryContainer != null ? rootEntryContainer.getFileSize() : 0);
        jobItemManager.insertDownloadJobItemsSync(Collections.singletonList(rootDownlaodJobItem));

        Map<Long, Long> contentEntryUidToDjiUidMap = new HashMap<>();
        List<Long> parentUids = new ArrayList<>();
        parentUids.add(contentEntryUid);
        contentEntryUidToDjiUidMap.put(contentEntryUid, rootDownlaodJobItem.getDjiUid());

        HashSet<Long> createdJoinCepjUids = new HashSet<>();
        do {
            childItemsToCreate = jobItemDao.findByParentContentEntryUuids(parentUids);
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "DownloadJobPreparer: found " +
                    childItemsToCreate.size() + " child items on from parents " +
                    UMUtil.debugPrintList(parentUids));

            parentUids.clear();

            for(DownloadJobItemDao.DownloadJobItemToBeCreated2 child : childItemsToCreate){
                if(!contentEntryUidToDjiUidMap.containsKey(child.getContentEntryUid())) {
                    DownloadJobItem newItem = new DownloadJobItem(downloadJobUid,
                            child.getContentEntryUid(), child.getContainerUid(), child.getFileSize());
                    jobItemManager.insertDownloadJobItemsSync(Arrays.asList(newItem));
                    numItemsCreated++;

                    contentEntryUidToDjiUidMap.put(child.getContentEntryUid(),
                            newItem.getDjiUid());

                    if(newItem.getDjiContainerUid() == 0) //this item is a branch, not a leaf if containeruid = 0
                        parentUids.add(child.getContentEntryUid());

                }

                if(!createdJoinCepjUids.contains(child.getCepcjUid())) {
                    jobItemManager.insertParentChildJoins(Arrays.asList(new DownloadJobItemParentChildJoin(
                            contentEntryUidToDjiUidMap.get(child.getParentEntryUid()),
                            contentEntryUidToDjiUidMap.get(child.getContentEntryUid()),
                            child.getCepcjUid())
                    ), null);
                    createdJoinCepjUids.add(child.getCepcjUid());
                }
            }

        }while(!parentUids.isEmpty());
        UstadMobileSystemImpl.l(UMLog.VERBOSE, 420, "Created " + numItemsCreated +
                " items. Time to prepare download job: " +
                (System.currentTimeMillis() - startTime) + "ms");
        CountDownLatch latch = new CountDownLatch(1);
        jobItemManager.commit((aVoid) -> latch.countDown());
        try { latch.await(5, TimeUnit.SECONDS); }
        catch(InterruptedException e) {}
    }
}
