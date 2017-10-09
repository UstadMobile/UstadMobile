package com.ustadmobile.core.catalog;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUtil;

import java.io.IOException;
import java.util.Vector;

import static com.ustadmobile.core.controller.CatalogPresenter.STATUS_ACQUIRED;

/**
 * Created by mike on 9/9/17.
 */

public class DirectoryScanner {

    /**
     * Flag for use with scanFiles: indicates that the feed acquisition links should be set using
     * with file uris
     */
    public static final int LINK_HREF_MODE_FILE = 0;

    /**
     * Flag for use with setLinkHrefMode: indicates that the feed acquisition links should be set
     * using ids e.g. baseHref/containerId
     */
    public static final int LINK_HREF_MODE_ID = 1;

    private int linkHrefMode;

    private String hrefModeBaseHref;

    public DirectoryScanner() {

    }

    /**
     *
     * @param directoryUri
     * @param cacheDirUri
     * @param feed A feed to add discovered entries to. Can be null, in which case a new feed object
     *             will be created.
     *
     * @return
     */
    public UstadJSOPDSFeed scanDirectory(String directoryUri, String cacheDirUri, String title,
                                         String feedId, int resourceMode,
                                         UstadJSOPDSItem.OpdsItemLoadCallback callback,
                                         UstadJSOPDSFeed feed, Object context){
        String baseHref = linkHrefMode == LINK_HREF_MODE_ID ? hrefModeBaseHref : directoryUri;
        if(feed == null)
            feed  = new UstadJSOPDSFeed(UMFileUtil.ensurePathHasPrefix(UMFileUtil.PROTOCOL_FILE, baseHref),
                title, feedId);

        try {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

            ContentTypePlugin[] supportedTypePlugins = ContentTypeManager.getSupportedContentTypePlugins();
            String[] dirContents = impl.listDirectory(directoryUri);
            if(dirContents == null) {
                //This directory does not exist - return null
                return null;
            }

            String fileExt;
            int j, k;
            UstadJSOPDSFeed fileFeed;
            UstadJSOPDSEntry entry;
            Vector acquisitionLinks;
            String fileUri;
            for(int i = 0; i < dirContents.length; i++) {
                fileExt = UMFileUtil.getExtension(dirContents[i]);
                fileUri = UMFileUtil.joinPaths(new String[]{directoryUri, dirContents[i]});
                if(fileExt == null)
                    continue;


                for(j = 0; j < supportedTypePlugins.length; j++) {
                    if(UMUtil.getIndexInArray(fileExt, supportedTypePlugins[j].getFileExtensions()) != -1) {
                        fileFeed = supportedTypePlugins[j].getEntry(fileUri, cacheDirUri);
                        if(fileFeed == null || fileFeed.size() < 1)
                            continue;//see if another plugin can handle it

                        for(k = 0; k < fileFeed.entries.length; k++) {
                            entry =new UstadJSOPDSEntry(feed, fileFeed.entries[k]);

                            //If this is a catalog being made to serve over HTTP : replace acquisition HREF with a base path followed by the ID
                            if(linkHrefMode == LINK_HREF_MODE_ID) {
                                acquisitionLinks = entry.getAcquisitionLinks();
                                if(acquisitionLinks != null && acquisitionLinks.size() > 0) {
                                    String[] links = (String[])acquisitionLinks.elementAt(0);
                                    links[UstadJSOPDSItem.ATTR_HREF] = UMFileUtil.joinPaths(new String[]{
                                            hrefModeBaseHref, entry.id});
                                }
                            }

                            feed.addEntry(entry);
                            if(callback != null)
                                callback.onEntryLoaded(feed, feed.size(), entry);

                            CatalogEntryInfo thisEntryInfo = CatalogPresenter.getEntryInfo(
                                    entry.id, resourceMode, context);
                            if(thisEntryInfo == null) {
                                impl.l(UMLog.VERBOSE, 409, dirContents[i]);
                                thisEntryInfo = new CatalogEntryInfo();
                                thisEntryInfo.acquisitionStatus = STATUS_ACQUIRED;
                                thisEntryInfo.fileURI = fileUri;
                                thisEntryInfo.mimeType = entry.getFirstAcquisitionLink(null)
                                        [UstadJSOPDSItem.ATTR_MIMETYPE];
                                thisEntryInfo.srcURLs = new String[] { dirContents[i] };


                                CatalogPresenter.setEntryInfo(entry.id, thisEntryInfo,
                                        resourceMode, context);
                                if(impl.getNetworkManager() != null)
                                    impl.getNetworkManager().handleEntryStatusChangeDiscovered(entry.id,
                                            thisEntryInfo.acquisitionStatus);
                            }

                            if(thisEntryInfo.acquisitionStatus != STATUS_ACQUIRED) {
                                thisEntryInfo.acquisitionStatus = STATUS_ACQUIRED;
                                CatalogPresenter.setEntryInfo(entry.id, thisEntryInfo,
                                        resourceMode, context);
                            }
                        }
                    }
                }
            }

        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 673, directoryUri, e);
        }


        return feed;
    }

    /**
     * When generating the output OPDS the acquisition link can be either the file path (default)
     * or it can be a set prefixed path followed by the ID (e.g. for use with the Http catalog
     * server where entries are retrieved using /catalog/entry/entry-id
     *
     * @see #LINK_HREF_MODE_FILE
     * @see #LINK_HREF_MODE_ID
     *
     * @return the current link href mode
     */
    public int getLinkHrefMode() {
        return linkHrefMode;
    }

    /**
     * When generating the output OPDS the acquisition link can be either the file path (default)
     * or it can be a set prefixed path followed by the ID (e.g. for use with the Http catalog
     * server where entries are retrieved using /catalog/entry/entry-id
     *
     * @see #LINK_HREF_MODE_FILE
     * @see #LINK_HREF_MODE_ID
     *
     * @param linkHrefMode the mode to use for any future calls to scanDirectory
     */
    public void setLinkHrefMode(int linkHrefMode) {
        this.linkHrefMode = linkHrefMode;
    }

    /**
     * If the link mode is set to LINK_HREF_MODE_ID then this is the prefix that will be given before
     * the id e.g if it is set to /catalog/entry/ and a given entry has the id 12345 the resulting
     * acquisition link would be /catalog/entry/12345
     *
     * @return The prefix if using LINK_HREF_MODE_ID
     */
    public String getHrefModeBaseHref() {
        return hrefModeBaseHref;
    }

    /**
     * If the link mode is set to LINK_HREF_MODE_ID then this is the prefix that will be given before
     * the id e.g if it is set to /catalog/entry/ and a given entry has the id 12345 the resulting
     * acquisition link would be /catalog/entry/12345
     *
     * @param hrefModeBaseHref the prefix to use if using LINK_HREF_MODE_ID
     */
    public void setHrefModeBaseHref(String hrefModeBaseHref) {
        this.hrefModeBaseHref = hrefModeBaseHref;
    }
}
