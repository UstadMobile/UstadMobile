package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.core.view.CatalogEntryView;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Created by mike on 4/20/17.
 */

public abstract class BaseCatalogPresenter extends UstadBaseController {


    public static final int CMD_REMOVE_ENTRIES = 53;

    /**
     * Preferred format list is kept as a comma separated string
     */
    public static final String PREF_KEY_FORMAT_PREFERENCES = "format_pref";

    public static final String ARG_URL = "url";

    public static final String ARG_BASE_HREF = "base";

    public static final String ARG_HTTPUSER = "httpu";

    public static final String ARG_HTTPPPASS = "httpp";

    public static final String ARG_RESMOD = "resmod";

    public static final String ARG_BOTTOM_BUTTON_URL = "b-btn-url";

    public static final String ARG_TITLE = "t";


    public BaseCatalogPresenter(Object context) {
        super(context);
    }


    public void onCreate(Hashtable savedState) {

    }

    /**
     * To be called when the user selects to download a list of entries.
     *
     * @param entriesToDownload List of OpdsEntryWithRelations objects to download
     */
    public void handleClickDownload(List<OpdsEntryWithRelations> entriesToDownload){
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UMStorageDir[] storageDirs = impl.getStorageDirs(CatalogPresenter.SHARED_RESOURCE, getContext());
        DownloadSet downloadJob = new DownloadSet();
        downloadJob.setDestinationDir(storageDirs[0].getDirURI());
        CrawlJob crawlJob = new CrawlJob();
        crawlJob.setQueueDownloadJobOnDone(true);

        //TODO: rework this to handle multiple selections, which would start multiple download jobs
        crawlJob.setRootEntryUuid(entriesToDownload.get(0).getUuid());

        impl.getNetworkManager().prepareDownloadAsync(downloadJob, crawlJob, true,
                (preparedCrawlJob) -> {
                    UstadMobileSystemImpl.l(UMLog.INFO, 0, "Download prepared");
                });
    }

    /**
     * Follow a link to a catalog entry. Use a URL that should point to an OPDS entry .
     *
     * @param entryUrl
     */
    public void handleOpenEntryView(String entryUrl) {
        Hashtable catalogEntryArgs = new Hashtable();
        catalogEntryArgs.put(ARG_URL, entryUrl);
        UstadMobileSystemImpl.getInstance().go(CatalogEntryView.VIEW_NAME, catalogEntryArgs,
                getContext());
    }


    public void appViewChoiceSelected(int commandId, int choice) {

    }

    /**
     * Must be overriden in the child class: here the child class should take of showing
     * progress indicators etc.
     */
    protected abstract void onDownloadStarted();

    /**
     * Must be overriden in the child class. Here the child class should take care of adjusting things
     * once entries have been downloaded
     *
     */
    protected abstract void onEntriesRemoved();


    protected String getAcquisitionStorageDir() {
        return UstadMobileSystemImpl.getInstance().getStorageDirs(CatalogPresenter.SHARED_RESOURCE,
                context)[0].getDirURI();
    }


    public static String[] getPreferredFormats(Object context) {
        String preferredFormats = UstadMobileSystemImpl.getInstance().getAppPref(PREF_KEY_FORMAT_PREFERENCES,
                CoreBuildConfig.DEFAULT_PREFERRED_ACQUISITION_FORMATS, context);
        Vector preferredFormatsVector = new Vector();
        UMUtil.tokenize(preferredFormats, new char[]{','}, 0, preferredFormats.length());
        String[] preferredForamtsArr = new String[preferredFormatsVector.size()];
        preferredFormatsVector.copyInto(preferredForamtsArr);
        return preferredForamtsArr;
    }

    protected void registerItemAcquisitionCompleted(String entryID) {
        //first lookup as a user storage only download: if it's not - see if it is a shared space download
        int entryAcquireResMode = CatalogPresenter.SHARED_RESOURCE;
        CatalogEntryInfo info = CatalogPresenter.getEntryInfo(entryID, CatalogPresenter.SHARED_RESOURCE,
                getContext());

        info.acquisitionStatus = CatalogPresenter.STATUS_ACQUIRED;
        CatalogPresenter.setEntryInfo(entryID, info, entryAcquireResMode, getContext());
    }

    /**
     * Make a message to dislpay to the user on the status of the download
     *
     * @param status The acquisitiontaskstatus object
     *
     * @return Formatted string to show user e.g. Downloading: XXXX KB/s
     */
    protected String formatDownloadStatusText(AcquisitionTaskStatus status) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        switch(status.getStatus()) {
            case UstadMobileSystemImpl.DLSTATUS_RUNNING:
                int kbpsSpeed = Math.round((float)status.getCurrentSpeed() / 1000f);
                return impl.getString(MessageID.downloading, getContext()) + ":"
                        + impl.formatInteger(kbpsSpeed) + " "
                        + impl.getString(MessageID.kilobytes_per_second_abbreviated, getContext());
            default:
                return "";
        }
    }

    /**
     * Make a message to dislpay to the user on the status of the download
     *
     * @param item The DownloadJobItem
     *
     * @return Formatted string to show user e.g. Downloading: XXXX KB/s
     */
    protected String formatDownloadStatusText(DownloadJobItem item) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        switch(item.getStatus()) {
            case UstadMobileSystemImpl.DLSTATUS_RUNNING:
                int kbpsSpeed = Math.round((float)item.getCurrentSpeed() / 1000f);
                return impl.getString(MessageID.downloading, getContext()) + ":"
                        + impl.formatInteger(kbpsSpeed) + " "
                        + impl.getString(MessageID.kilobytes_per_second_abbreviated, getContext());
            default:
                return "";
        }
    }


}
