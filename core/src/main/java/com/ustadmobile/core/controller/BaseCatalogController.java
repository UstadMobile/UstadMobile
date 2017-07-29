package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.ContainerView;

import java.util.Enumeration;
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

    }

    /**
     *
     * @param
     */
    public void handleClickDownload(UstadJSOPDSFeed acquisitionFeed, Vector selectedEntries, String[] languageChoices, boolean forceShowLanguageChoice) {
        this.acquisitionFeedSelected = acquisitionFeed;
        this.acquisitionEntriesSelected = selectedEntries;
        this.acquisitionLanguageChoices = languageChoices;


        if(languageChoices.length == 1 && !forceShowLanguageChoice){
            appViewChoiceSelected(CMD_CHOOSE_LANG_DOWNLOAD, 0);
        }else {
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
        }
    }

    public void handleClickDownload(UstadJSOPDSFeed acquisitionFeed, Vector selectedEntries) {
        Vector availableLanguages = acquisitionFeed.getAvailableLanguages();
        acquisitionLanguageChoices = new String[availableLanguages.size()];
        availableLanguages.copyInto(acquisitionLanguageChoices);

        handleClickDownload(acquisitionFeed, selectedEntries, acquisitionLanguageChoices, false);
    }



    public void handleClickRemove(UstadJSOPDSEntry[] entries) {
        this.removeEntriesSelected = entries;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getAppView(context).showChoiceDialog(impl.getString(MessageID.delete_q, getContext()),
                new String[]{impl.getString(MessageID.ok, getContext()),
                        impl.getString(MessageID.cancel, getContext())},
                        CMD_REMOVE_ENTRIES, this);

    }

    protected Hashtable getTranslatedAlternatives(UstadJSOPDSEntry entry){
        Hashtable candidateEntries = new Hashtable();
        candidateEntries.put(entry.getLanguage() != null ? entry.getLanguage() : "", entry);
        String[] translatedEntryIds = entry.getAlternativeTranslationEntryIds();
        for(int i = 0; i < translatedEntryIds.length; i++) {
            UstadJSOPDSEntry translatedEntry = entry.parentFeed.getEntryById(translatedEntryIds[i]);
            if(translatedEntry != null && translatedEntry.getLanguage() != null)
                candidateEntries.put(translatedEntry.getLanguage(), translatedEntry);

        }

        return candidateEntries;
    }

    /**
     * Returns a hashtable in the form of language - entry id for the entry and any known translations
     *
     * @param entry
     *
     * @return
     */
    protected Hashtable getTranslatedAlternatives(UstadJSOPDSEntry entry, int acquisitionStatus) {
        Hashtable candidateEntries = getTranslatedAlternatives(entry);
        Hashtable matchingEntries = new Hashtable();


        CatalogEntryInfo info;
        UstadJSOPDSEntry candidateEntry;
        String candidateLang;
        Enumeration candidateLangs = candidateEntries.keys();
        while(candidateLangs.hasMoreElements()) {
            candidateLang = (String)candidateLangs.nextElement();
            candidateEntry = (UstadJSOPDSEntry)candidateEntries.get(candidateLang);
            info = CatalogController.getEntryInfo(candidateEntry.id,
                    CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE,
                    getContext());
            if(info != null && info.acquisitionStatus == acquisitionStatus) {
                matchingEntries.put(candidateLang, candidateEntry);
            }else if(info == null && acquisitionStatus == CatalogController.STATUS_NOT_ACQUIRED) {
                matchingEntries.put(candidateLang, candidateEntry);
            }
        }

        return matchingEntries;
    }

    /**
     * Prepares two vectors in corresponding order. The first vector is a list of langauges in which
     * the item is available. The second vector is a list of the corresponding entry ids.
     *
     * @param entry
     * @param acquisitionStatus
     *
     * @return
     */
    protected Vector[] getTranslatedAlternativesLangVectors(UstadJSOPDSEntry entry, int acquisitionStatus) {
        Vector langNameVector= new Vector();
        Vector entryIdVector = new Vector();

        Hashtable translatedAlternativesTable = getTranslatedAlternatives(entry, acquisitionStatus);
        Enumeration langCodesE = translatedAlternativesTable.keys();

        Object langNameObj;
        String langCode;
        while(langCodesE.hasMoreElements()) {
            langCode = (String)langCodesE.nextElement();
            langNameObj = UstadMobileConstants.LANGUAGE_NAMES.get(langCode);
            langNameVector.addElement(langNameObj != null ? langNameObj : langCode);
            entryIdVector.addElement(translatedAlternativesTable.get(langCode));
        }

        return new Vector[]{langNameVector, entryIdVector};
    }


    public void handleClickOpenEntry(UstadJSOPDSEntry entry) {
        Vector[] langOptionsVectors = getTranslatedAlternativesLangVectors(entry,
                CatalogController.STATUS_ACQUIRED);

        if(langOptionsVectors[0].size() == 0) {
            UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                "Error: entry not acquired", AppView.LENGTH_LONG);
        }else if(langOptionsVectors[0].size() == 1) {
            handleOpenEntry((UstadJSOPDSEntry)langOptionsVectors[1].elementAt(0));
        }else {
            String[] availableLanguages = new String[langOptionsVectors[0].size()];
            langOptionsVectors[0].copyInto(availableLanguages);

            final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            this.openEntriesAvailable = langOptionsVectors[1];
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
                acquisitionFeed.addLink(NetworkManagerCore.LINK_REL_DOWNLOAD_DESTINATION,
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
