package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * Created by mike on 3/5/18.
 */

public class DownloadDialogPresenter extends UstadBaseController<DownloadDialogView> {

    public static final String ARG_ROOT_URIS = "r_uris";

    public static final String ARG_ROOT_UUID = "r_uuids";

    private UmAppDatabase dbManager;

    private UmLiveData<OpdsEntryWithStatusCache> rootEntryLiveData;

    private String[] rootEntryUuid;

    private String rootEntryId;

    private UmLiveData<OpdsEntryWithStatusCache> entryLiveData;

    private UmLiveData<CrawlJobWithTotals> crawlJobLiveData;

    private UmLiveData<DownloadJobWithTotals> downloadJobLiveData;

    CrawlJobWithTotals crawlJob;

    private int crawlJobId;

    private int downloadJobId;

    public static final int OPTION_START_DOWNLOAD = 1;

    public static final int OPTION_PAUSE_DOWNLOAD = 2;

    public static final int OPTION_CANCEL_DOWNLOAD = 4;

    public static final int OPTION_RESUME_DOWNLOAD = 8;

    public static final int OPTION_DELETE = 16;

    private int selectedOption = 0;

    private boolean wifiOnly = true;

    private int optionsAvailable;

    public DownloadDialogPresenter(Object context, DownloadDialogView view, Hashtable args) {
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        dbManager = UmAppDatabase.getInstance(getContext());

    }

    private void observeRootUuid() {

    }


//    public void onEntryChanged(OpdsEntryWithStatusCache entry) {
//        int optionsAvailable = 0;
//
//        if(entry == null)
//            return;
//
//        rootEntryUuid[0] = entry.getUuid();
//        rootEntryId = entry.getEntryId();
//        OpdsEntryStatusCache status = entry.getStatusCache();
//
//        if(status == null)
//            return;//has not really loaded yet
//
//        boolean inProgress = entry.getDownloadDisplayState() == OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_IN_PROGRESS;
//        boolean paused = entry.getDownloadDisplayState() == OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_PAUSED;
//
//        if(!inProgress && entry.getStatusCache().getContainersDownloadedIncDescendants() > 0) {
//            optionsAvailable = optionsAvailable | OPTION_DELETE;
//        }
//
//        if(!(inProgress || paused) && (status.getContainersDownloadedIncDescendants() == 0 ||
//                (status.getContainersDownloadedIncDescendants() + status.getContainersDownloadPendingIncAncestors()) < status.getEntriesWithContainerIncDescendants())){
//            optionsAvailable = optionsAvailable | OPTION_START_DOWNLOAD;
//        }
//
//        if(inProgress) {
//            optionsAvailable = optionsAvailable | OPTION_CANCEL_DOWNLOAD;
//            optionsAvailable = optionsAvailable | OPTION_PAUSE_DOWNLOAD;
//        }
//
//        if(paused) {
//            optionsAvailable = optionsAvailable | OPTION_RESUME_DOWNLOAD;
//            optionsAvailable = optionsAvailable | OPTION_CANCEL_DOWNLOAD;
//        }
//
//        this.optionsAvailable = optionsAvailable;
//        int numOptions = Integer.bitCount(optionsAvailable);
//        view.setAvailableOptions(optionsAvailable, numOptions > 1);
//        if(numOptions == 1)
//            handleSelectOption(optionsAvailable);
//    }

}
