package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.CatalogEntryView;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by mike on 4/20/17.
 */

public abstract class BaseCatalogPresenter extends UstadBaseController implements AppViewChoiceListener  {


    public static final int CMD_REMOVE_ENTRIES = 53;

    /**
     * Preferred format list is kept as a comma separated string
     */
    public static final String PREF_KEY_FORMAT_PREFERENCES = "format_pref";

    protected String[] acquisitionLanguageChoices;

    protected UstadJSOPDSFeed acquisitionFeedSelected;

    protected Vector acquisitionEntriesSelected;

    protected UstadJSOPDSEntry[] removeEntriesSelected;

    public static final String ARG_URL = "url";

    public static final String ARG_BASE_HREF = "base";

    public static final String ARG_HTTPUSER = "httpu";

    public static final String ARG_HTTPPPASS = "httpp";

    public static final String ARG_RESMOD = "resmod";

    public static final String ARG_BOTTOM_BUTTON_URL = "b-btn-url";

    public static final String ARG_TITLE = "t";

    public interface AcquisitionChoicesCompletedCallback {

        void onChoicesCompleted(UstadJSOPDSFeed preparedFeed);

    }


    public BaseCatalogPresenter(Object context, boolean statusEventListeningEnabled) {
        super(context, statusEventListeningEnabled);
    }

    public BaseCatalogPresenter(Object context) {
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

        UstadJSOPDSFeed selectedFeed = acquisitionFeedSelected.selectAcquisitionLinks(
                acquisitionEntriesSelected, getPreferredFormats(context),
                null,
                CoreBuildConfig.ACQUISITION_SELECT_WEIGHT_MIMETYPE,
                CoreBuildConfig.ACQUISITION_SELECT_WEIGHT_LANGUAGE);
        selectedFeed.addLink(NetworkManagerCore.LINK_REL_DOWNLOAD_DESTINATION,
                "application/dir", getAcquisitionStorageDir());


        UstadMobileSystemImpl.getInstance().getNetworkManager().requestAcquisition(
                selectedFeed, true, true);
        onDownloadStarted();
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
            UstadJSOPDSEntry translatedEntry = entry.getParentFeed().getEntryById(translatedEntryIds[i]);
            if(translatedEntry != null && translatedEntry.getLanguage() != null)
                candidateEntries.put(translatedEntry.getLanguage(), translatedEntry);

        }

        return candidateEntries;
    }

    /**
     * Returns a hashtable in the form of language - entry uuid for the entry and any known translations
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
            info = CatalogPresenter.getEntryInfo(candidateEntry.getItemId(),
                    CatalogPresenter.ALL_RESOURCES, getContext());
            if(info != null && info.acquisitionStatus == acquisitionStatus) {
                matchingEntries.put(candidateLang, candidateEntry);
            }else if(info == null && acquisitionStatus == CatalogPresenter.STATUS_NOT_ACQUIRED) {
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

    /**
     * Open the given entry in the catalog entry view
     *
     * @param titlebarText Where the 'normal' thumbnail display mode is used for a catalog entry,
     *                    this sets the title bar. This is normally the same as the OPDS catalog
     *                     title it was navigated from. The title of the entry itself is displayed
     *                     next to the thumbnail. Where a banner is used this parameter is ignored.
     * @param entry The OPDS entry to navigate open.
     */
    public void handleOpenEntryView(UstadJSOPDSEntry entry, String titlebarText) {
        Hashtable catalogEntryArgs = new Hashtable();
        UstadJSOPDSFeed parentFeed = entry.getParentFeed();
        UmOpdsLink entryAbsoluteLink = entry.getParentFeed().getAbsoluteSelfLink();
        if(entryAbsoluteLink == null && entry.getParentFeed().getHref() != null) {
            parentFeed.addLink(UstadJSOPDSFeed.LINK_REL_SELF_ABSOLUTE,
                    parentFeed.isAcquisitionFeed() ? UstadJSOPDSFeed.TYPE_ACQUISITIONFEED
                    : UstadJSOPDSFeed.TYPE_NAVIGATIONFEED, parentFeed.getHref());
        }

        catalogEntryArgs.put(CatalogEntryPresenter.ARG_ENTRY_OPDS_STR,
                entry.getParentFeed().serializeToString(true));
        catalogEntryArgs.put(CatalogEntryPresenter.ARG_ENTRY_ID,
                entry.getItemId());
        if(titlebarText != null)
            catalogEntryArgs.put(CatalogEntryPresenter.ARG_TITLEBAR_TEXT, titlebarText);

        UstadMobileSystemImpl.getInstance().go(CatalogEntryView.VIEW_NAME, catalogEntryArgs,
                getContext());
    }

    /**
     * Open the given entry in the catalog entry view
     *
     * @param entry The OPDS entry to navigate open.
     */
     public void handleOpenEntryView(UstadJSOPDSEntry entry) {
        handleOpenEntryView(entry, null);
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
        switch(commandId) {
            case CMD_REMOVE_ENTRIES:
                for(int i = 0; i < removeEntriesSelected.length; i++) {
                    CatalogPresenter.removeEntry(removeEntriesSelected[i].getItemId(),
                            CatalogPresenter.ALL_RESOURCES, getContext());
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
     * Utility method to find the language names for available alternative language versions
     *
     * @param languageLinks
     * @return
     */
    protected String[] getNamesForLangaugeCodes(Vector languageLinks) {
        String[] retVal = new String[languageLinks.size()];
        Object currentVal;
        String[] links;
        String langCode;
        for(int i = 0; i < languageLinks.size(); i++) {
            currentVal = languageLinks.elementAt(i);
            if(currentVal instanceof String[]) {
                links = (String[])currentVal;
                langCode = links[UstadJSOPDSItem.ATTR_HREFLANG];

            }else {
                langCode = (String)currentVal;
            }
            retVal[i] = UstadMobileConstants.LANGUAGE_NAMES.containsKey(langCode) ?
                    (String)UstadMobileConstants.LANGUAGE_NAMES.get(langCode) : langCode;
        }

        return retVal;
    }


}
