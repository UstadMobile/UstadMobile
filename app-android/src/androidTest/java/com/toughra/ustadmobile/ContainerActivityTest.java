package com.toughra.ustadmobile;

import android.test.ActivityInstrumentationTestCase2;

import com.ustadmobile.port.android.view.ContainerActivity;

/**
 * Created by mike on 9/23/15.
 */
public abstract class ContainerActivityTest extends ActivityInstrumentationTestCase2<ContainerActivity> {


    public static final int TIMEOUT = 2* 60 * 1000;

    public static final int CHECKINTERVAL = 1000;

    public static final int SLEEP_AFTER = 10000;

    public ContainerActivityTest() {
        super(ContainerActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        /*
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        String httpRoot = TestUtils.getInstance().getHTTPRoot();


        impl.checkCacheDir();
        String acquireOPDSURL = UMFileUtil.joinPaths(new String[]{
                httpRoot, "acquire.opds"});
        UstadJSOPDSFeed feed = CatalogController.getCatalogByURL(acquireOPDSURL,
                CatalogController.SHARED_RESOURCE, null, null,
                CatalogController.CACHE_ENABLED);

        CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(feed.entries[0].id,
                CatalogController.SHARED_RESOURCE);
        boolean entryPresent = entryInfo != null && entryInfo.acquisitionStatus == CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
        if(!entryPresent) {
            UstadMobileSystemImpl.l(UMLog.INFO, 371, "ContainerActivityTest downloading resource");
            UMStorageDir[] dirs = impl.getStorageDirs(CatalogController.SHARED_RESOURCE);
            CatalogController.AcquireRequest request = new CatalogController.AcquireRequest(
                    feed.entries, dirs[0].getDirURI(), null, null, CatalogController.SHARED_RESOURCE);

            UMTransferJob acquireJob = CatalogController.acquireCatalogEntries(request);
            int totalSize = acquireJob.getTotalSize();

            acquireJob.start();
            int timeRemaining = TIMEOUT;
            while(timeRemaining > 0 && !acquireJob.isFinished()) {
                try {Thread.sleep(CHECKINTERVAL); }
                catch(InterruptedException e) {}
                timeRemaining -= CHECKINTERVAL;
                UstadMobileSystemImpl.l(UMLog.INFO, 371,
                    "ContainerActivityTest waiting for download: time remaining " + timeRemaining + "ms");
            }

            UstadMobileSystemImpl.l(UMLog.INFO, 371, "ContainerActivityTest downloading resource complete: " + acquireJob.isFinished());
        }else {
            UstadMobileSystemImpl.l(UMLog.INFO, 371, "ContainerActivityTest resource already present");
        }

        entryInfo = CatalogController.getEntryInfo(feed.entries[0].id, CatalogController.SHARED_RESOURCE);
        if(entryInfo == null || entryInfo.acquisitionStatus != CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED) {
            throw new IllegalStateException("Could not acquire resource for the test!");
        }

        Intent intent = new Intent();
        intent.putExtra(ContainerController.ARG_CONTAINERURI, entryInfo.fileURI);
        intent.putExtra(ContainerController.ARG_MIMETYPE, entryInfo.mimeType);
        setActivityIntent(intent);
        */
    }

    public void testContainerActivity() {
        /*
        assertNotNull(getActivity());
        ContainerViewAndroid viewAndroid = ((ContainerActivity)getActivity()).getContainerView();
        int timeRemaining = TIMEOUT;
        while(timeRemaining > 0 && viewAndroid.getContainerController() == null) {
            try {Thread.sleep(CHECKINTERVAL); }
            catch(InterruptedException e) {}
        }
        assertNotNull(viewAndroid.getContainerController());
        UstadMobileSystemImpl.l(UMLog.INFO, 371, "ContainerActivityTest complete");
        /*
         * Not sleeping here causes an illegalstateexception in Android 2.3: what seems to happen
         * is that the system calls onSaveInstanceState... then the catalog itself loads and then
         * we have trouble - the activity is already over.
         */
        try { Thread.sleep(SLEEP_AFTER); }
        catch(InterruptedException e) {}
    }


}
