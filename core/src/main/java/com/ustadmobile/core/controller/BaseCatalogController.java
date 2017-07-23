package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.ContainerView;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by mike on 4/20/17.
 */

public abstract class BaseCatalogController extends UstadBaseController implements AppViewChoiceListener  {

    public static final int CMD_CHOOSE_LANG_DOWNLOAD = 52;

    public static final int CMD_REMOVE_ENTRIES = 53;

    public static final int CMD_CHOOSE_LANG_OPEN = 54;

    /**
     * Preferred format list is kept as a comma separated string
     */
    public static final String PREF_KEY_FORMAT_PREFERENCES = "format_pref";

    protected String[] acquisitionLanguageChoices;

    protected UstadJSOPDSFeed acquisitionFeedSelected;

    protected Vector acquisitionEntriesSelected;

    protected UstadJSOPDSEntry[] removeEntriesSelected;

    protected Vector openEntriesAvailable;

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
        //AcquisitionManager.getInstance().registerEntryAquisitionStatusListener(this, context);
    }

    /**
     *
     * @param
     */
    public void handleClickDownload(UstadJSOPDSFeed acquisitionFeed, Vector selectedEntries) {
        this.acquisitionFeedSelected = acquisitionFeed;
        this.acquisitionEntriesSelected = selectedEntries;

        Vector availableLanguages = acquisitionFeed.getAvailableLanguages();
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

            impl.getAppView(context).showChoiceDialog(
                    impl.getString(MessageID.language, getContext()),
                    displayLanguages, CMD_CHOOSE_LANG_DOWNLOAD, this);
        }else if(availableLanguages.size() == 1){
            appViewChoiceSelected(CMD_CHOOSE_LANG_DOWNLOAD, 0);
        }
    }

    public void handleClickRemove(UstadJSOPDSEntry[] entries) {
        this.removeEntriesSelected = entries;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getAppView(context).showChoiceDialog(impl.getString(MessageID.delete_q, getContext()),
                new String[]{impl.getString(MessageID.ok, getContext()),
                        impl.getString(MessageID.cancel, getContext())},
                        CMD_REMOVE_ENTRIES, this);

    }

    public void handleClickOpenEntry(UstadJSOPDSEntry entry) {
        Vector candidateEntries = new Vector();
        Vector acquiredEntries = new Vector();

        candidateEntries.addElement(entry);
        String[] translatedEntryIds = entry.getAlternativeTranslationEntryIds();
        for(int i = 0; i < translatedEntryIds.length; i++) {
            UstadJSOPDSEntry translatedEntry = entry.parentFeed.getEntryById(translatedEntryIds[i]);
            if(translatedEntry != null)
                candidateEntries.addElement(translatedEntry);

        }

        CatalogEntryInfo info;
        UstadJSOPDSEntry candidateEntry;
        for(int i = 0; i < candidateEntries.size(); i++) {
            candidateEntry = (UstadJSOPDSEntry)candidateEntries.elementAt(i);
            info = CatalogController.getEntryInfo(candidateEntry.id,
                    CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE,
                    getContext());
            if(info != null && info.acquisitionStatus == CatalogController.STATUS_ACQUIRED)
                acquiredEntries.addElement(candidateEntry);
        }

        if(acquiredEntries.size() == 0) {
            UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                "Error: entry not acquired", AppView.LENGTH_LONG);
        }else if(acquiredEntries.size() == 1) {
            handleOpenEntry((UstadJSOPDSEntry)acquiredEntries.elementAt(0));
        }else {
            String[] availableLanguages = new String[acquiredEntries.size()];
            Object candidateLangObj;
            for(int i = 0; i < availableLanguages.length; i++) {
                candidateEntry = (UstadJSOPDSEntry)acquiredEntries.elementAt(i);
                candidateLangObj = UstadMobileConstants.LANGUAGE_NAMES.get(
                        candidateEntry.getLanguage());
                availableLanguages[i] = candidateLangObj != null ? (String)candidateLangObj :
                        candidateEntry.getLanguage();
            }
            final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            this.openEntriesAvailable = acquiredEntries;
            UstadMobileSystemImpl.getInstance().getAppView(getContext()).showChoiceDialog(
                    impl.getString(MessageID.select_language, getContext()), availableLanguages,
                    CMD_CHOOSE_LANG_OPEN, this);
        }
    }

    public void handleOpenEntry(UstadJSOPDSEntry entry) {
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
            case CMD_CHOOSE_LANG_DOWNLOAD:
                UstadJSOPDSFeed acquisitionFeed = acquisitionFeedSelected.selectAcquisitionLinks(
                        acquisitionEntriesSelected, getPreferredFormats(context),
                        new String[]{acquisitionLanguageChoices[choice]},
                        CoreBuildConfig.ACQUISITION_SELECT_WEIGHT_MIMETYPE,
                        CoreBuildConfig.ACQUISITION_SELECT_WEIGHT_LANGUAGE);
                acquisitionFeed.addLink(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                        "application/dir", getAcquisitionStorageDir());


                UstadMobileSystemImpl.getInstance().getNetworkManager().requestAcquisition(
                    acquisitionFeed, true, true);
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

            case CMD_CHOOSE_LANG_OPEN:
                handleOpenEntry((UstadJSOPDSEntry)openEntriesAvailable.elementAt(choice));
                openEntriesAvailable = null;
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
