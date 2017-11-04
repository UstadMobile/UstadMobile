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
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import static com.ustadmobile.core.controller.CatalogPresenter.STATUS_ACQUIRED;
import static com.ustadmobile.core.controller.CatalogPresenter.sanitizeIDForFilename;

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
            ContentTypePlugin.EntryResult entryResult;
            UstadJSOPDSFeed fileFeed;
            UstadJSOPDSFeed linkFeed;
            InputStream linkFeedIn = null;

            UstadJSOPDSEntry entry;
            Vector acquisitionLinks;
            String fileUri;
            String linkFeedUri;
            String cacheEntryUri;
            InputStream thumbnailData = null;
            OutputStream thumbnailOut = null;

            for(int i = 0; i < dirContents.length; i++) {
                fileExt = UMFileUtil.getExtension(dirContents[i]);
                fileUri = UMFileUtil.joinPaths(new String[]{directoryUri, dirContents[i]});


                if(fileExt == null)
                    continue;

                cacheEntryUri = UMFileUtil.removeExtension(dirContents[i]) + ".opds";

                for(j = 0; j < supportedTypePlugins.length; j++) {
                    if(UMUtil.getIndexInArray(fileExt, supportedTypePlugins[j].getFileExtensions()) != -1) {
                        entryResult = supportedTypePlugins[j].getEntry(fileUri, cacheDirUri);

                        if(entryResult == null || entryResult.getFeed().size() < 1)
                            continue;//see if another plugin can handle it

                        fileFeed = entryResult.getFeed();

                        linkFeedUri = UMFileUtil.joinPaths(new String[]{directoryUri, dirContents[i]
                                + ".links.opds"});
                        linkFeed = null;

                        if(impl.fileExists(linkFeedUri)) {
                            linkFeed = new UstadJSOPDSFeed();
                            try {
                                linkFeedIn = impl.openFileInputStream(linkFeedUri);
                                linkFeed.loadFromXpp(impl.newPullParser(linkFeedIn), null);
                            }catch(XmlPullParserException x) {
                                UstadMobileSystemImpl.l(UMLog.ERROR, 688, linkFeedUri, x);
                            }catch(IOException e) {
                                UstadMobileSystemImpl.l(UMLog.ERROR, 688, linkFeedUri, e);
                            }finally {
                                UMIOUtils.closeInputStream(linkFeedIn);
                            }
                        }

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

                            if(entryResult.getThumbnailMimeType() != null) {
                                try {
                                    thumbnailData = entryResult.getThumbnail();
                                    if(thumbnailData == null)
                                        throw new FileNotFoundException();

                                    String extension = UstadMobileSystemImpl.getInstance()
                                            .getExtensionFromMimeType(entryResult.getThumbnailMimeType());
                                    String thumbnailFilename = entry.id + "-thumb." + extension;
                                    String thumbnailAbsolutePath = UMFileUtil.joinPaths(
                                            new String[]{cacheDirUri, thumbnailFilename});
                                    thumbnailOut = impl.openFileOutputStream(thumbnailAbsolutePath, 0);
                                    UMIOUtils.readFully(thumbnailData, thumbnailOut, 8 * 1024);
                                    entry.addLink(UstadJSOPDSItem.LINK_REL_THUMBNAIL,
                                            entryResult.getThumbnailMimeType(), thumbnailAbsolutePath);
                                }catch(IOException e) {
                                    UstadMobileSystemImpl.l(UMLog.ERROR, 688, null, e);
                                }finally {
                                    UMIOUtils.closeInputStream(thumbnailData);
                                    UMIOUtils.closeOutputStream(thumbnailOut);
                                    thumbnailData = null;
                                    thumbnailOut = null;
                                }
                            }

                            if(linkFeed != null && linkFeed.getEntryById(entry.id) != null) {
                                Vector allLinks = linkFeed.getEntryById(entry.id).getLinks();
                                for(int l = 0; l < allLinks.size(); l++) {
                                    entry.addLink((String[])allLinks.elementAt(l));
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
