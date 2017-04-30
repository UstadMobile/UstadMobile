package com.ustadmobile.core.controller;

import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusListener;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.ContainerView;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by mike on 4/20/17.
 */

public abstract class BaseCatalogController extends UstadBaseController implements AppViewChoiceListener, AcquisitionStatusListener {

    public static final int CMD_CHOOSE_LANG = 52;

    public static final int CMD_REMOVE_ENTRIES = 53;

    /**
     * Preferred format list is kept as a comma separated string
     */
    public static final String PREF_KEY_FORMAT_PREFERENCES = "format_pref";

    protected String[] acquisitionLanguageChoices;

    protected UstadJSOPDSFeed acquisitionFeedSelected;

    protected UstadJSOPDSEntry[] removeEntriesSelected;

    public interface AcquisitionChoicesCompletedCallback {

        void onChoicesCompleted(UstadJSOPDSFeed preparedFeed);

    }


    public BaseCatalogController(Object context, boolean statusEventListeningEnabled) {
        super(context, statusEventListeningEnabled);
    }

    public BaseCatalogController(Object context) {
        super(context);
    }


    public void onCreate(Hashtable savedState) {
        AcquisitionManager.getInstance().registerEntryAquisitionStatusListener(this, context);
    }

    /**
     *
     * @param acquisitionFeed
     */
    public void handleClickDownload(UstadJSOPDSFeed acquisitionFeed) {
        this.acquisitionFeedSelected = acquisitionFeed;
        Vector availableLanguages = acquisitionFeed.getLinkHrefLanguageOptions(
                UstadJSOPDSItem.LINK_ACQUIRE, null, true, true, null);
        acquisitionLanguageChoices = new String[availableLanguages.size()];
        availableLanguages.copyInto(acquisitionLanguageChoices);
        if(availableLanguages.size() > 1) {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            String[] displayLanguages = new String[acquisitionLanguageChoices.length];
            Object displayLangVal;
            for(int i = 0; i < acquisitionLanguageChoices.length; i++) {
                displayLangVal = UstadMobileConstants.LANGUAGE_NAMES.get(acquisitionLanguageChoices[i]);
                if(displayLangVal != null)
                    displayLanguages[i] = (String)displayLangVal;
                else
                    displayLanguages[i] = acquisitionLanguageChoices[i];
            }

            impl.getAppView(context).showChoiceDialog(impl.getString(MessageIDConstants.language),
                displayLanguages, CMD_CHOOSE_LANG, this);
        }else if(availableLanguages.size() == 1){
            appViewChoiceSelected(CMD_CHOOSE_LANG, 0);
        }
    }

    public void handleClickRemove(UstadJSOPDSEntry[] entries) {
        this.removeEntriesSelected = entries;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getAppView(context).showChoiceDialog(impl.getString(MessageIDConstants.delete_q),
                new String[]{impl.getString(MessageIDConstants.ok), impl.getString(MessageIDConstants.cancel)},
                        CMD_REMOVE_ENTRIES, this);

    }

    public void handleClickOpenEntry(UstadJSOPDSEntry entry) {
        CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id,
                CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, getContext());
        if (entryInfo != null && entryInfo.acquisitionStatus == CatalogController.STATUS_ACQUIRED) {
            Hashtable openArgs = new Hashtable();
            openArgs.put(ContainerController.ARG_CONTAINERURI, entryInfo.fileURI);
            openArgs.put(ContainerController.ARG_MIMETYPE, entryInfo.mimeType);
            openArgs.put(ContainerController.ARG_OPFINDEX, new Integer(0));
            UstadMobileSystemImpl.getInstance().go(ContainerView.VIEW_NAME, openArgs, getContext());
        }
    }



    public void appViewChoiceSelected(int commandId, int choice) {
        switch(commandId) {
            case CMD_CHOOSE_LANG:
                UstadJSOPDSFeed acquisitionFeed = acquisitionFeedSelected.selectAcquisitionLinks(
                        getPreferredFormats(context), new String[]{acquisitionLanguageChoices[choice]},
                        CoreBuildConfig.ACQUISITION_SELECT_WEIGHT_MIMETYPE,
                        CoreBuildConfig.ACQUISITION_SELECT_WEIGHT_LANGUAGE);
                acquisitionFeed.addLink(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                        "application/dir", getAcquisitionStorageDir());

                AcquisitionManager.getInstance().acquireCatalogEntries(acquisitionFeed, context);
                onDownloadStarted();
                break;

            case CMD_REMOVE_ENTRIES:
                for(int i = 0; i < removeEntriesSelected.length; i++) {
                    CatalogController.removeEntry(removeEntriesSelected[i].id,
                            CatalogController.USER_RESOURCE | CatalogController.SHARED_RESOURCE,
                            getContext());
                }
                onEntriesRemoved();
                break;
        }
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
        return UstadMobileSystemImpl.getInstance().getStorageDirs(CatalogController.SHARED_RESOURCE,
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
        int entryAcquireResMode = CatalogController.SHARED_RESOURCE;
        CatalogEntryInfo info = CatalogController.getEntryInfo(entryID, CatalogController.SHARED_RESOURCE,
                getContext());

        info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
        CatalogController.setEntryInfo(entryID, info, entryAcquireResMode, getContext());
    }


}
