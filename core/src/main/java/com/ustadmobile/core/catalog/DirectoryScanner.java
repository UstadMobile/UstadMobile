package com.ustadmobile.core.catalog;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.util.UMUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import static com.ustadmobile.core.controller.CatalogPresenter.STATUS_ACQUIRED;

/**
 * Created by mike on 9/9/17.
 */

public class DirectoryScanner {

    /**
     * Flag to indicate that links added (acquisition and thumbnail) should be absolute file URLs in
     * the form of file:///path/to/file.ext
     */
    public static final int LINK_HREF_MODE_ABSOLUTE = 0;

    /**
     * Flag to indicate that links added (acquisition and thumbnail) should be relative file names
     * e.g. file.ext
     */
    public static final int LINK_HREF_MODE_RELATIVE = 2;


    /**
     * Flag to indicate that links added (acquisition and thumbnail) should be added using the
     * entry ID and not the actual filename
     */
    public static final int LINK_HREF_MODE_ID = 1;

    private int linkHrefMode;

    private String acquisitionLinkHrefPrefix;

    private int thumbnailHrefMode;

    private String entryThumbnailLinkHrefPrefix;

    public DirectoryScanner() {

    }

    /**
     * Scan the given directory for content of supported types and return an OPDS feed of content
     * found.
     *
     * @param directoryUri The directory to scan
     * @param cacheDirUri The directory to use for thumbnails (possibly in future to cache extracted opds
     * @param feed A feed to add discovered entries to. Can be null, in which case a new feed object
     *             will be created.
     * @param resourceMode When entries are scanned they will be added to the catalogentryinfo database.
     *         This parameter will determine if they are deemed to be system wide shared resources
     *         or user specific resources.
     *
     * @return UstadJSOPDSFeed object with entries for each item found in the given directory.
     */
    public UstadJSOPDSFeed scanDirectory(String directoryUri, String cacheDirUri, String title,
                                         String feedId, int resourceMode,
                                         UstadJSOPDSItem.OpdsItemLoadCallback callback,
                                         UstadJSOPDSFeed feed, Object context){
//        String baseHref = linkHrefMode == LINK_HREF_MODE_ID ? acquisitionLinkHrefPrefix : directoryUri;
//        if(feed == null)
//            feed  = new UstadJSOPDSFeed(UMFileUtil.ensurePathHasPrefix(UMFileUtil.PROTOCOL_FILE, baseHref),
//                title, feedId);
//
//        try {
//            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//
//            ContentTypePlugin[] supportedTypePlugins = UstadMobileSystemImpl.getInstance().getSupportedContentTypePlugins();
//            String[] dirContents = impl.listDirectory(directoryUri);
//            if(dirContents == null) {
//                //This directory does not exist - return null
//                return null;
//            }
//
//            String fileExt;
//            String containerLinkHref;
//            int j, k;
//            ContentTypePlugin.EntryResult entryResult;
//            UstadJSOPDSFeed fileFeed;
//            UstadJSOPDSFeed linkFeed;
//            InputStream linkFeedIn = null;
//
//            UstadJSOPDSEntry entry;
//            UmOpdsLink acquisitionLink;
//
//            String fileUri;
//            String linkFeedUri;
//            String cacheEntryUri;
//            InputStream thumbnailData = null;
//            OutputStream thumbnailOut = null;
//
//            for(int i = 0; i < dirContents.length; i++) {
//                fileExt = UMFileUtil.getExtension(dirContents[i]);
//                fileUri = UMFileUtil.joinPaths(new String[]{directoryUri, dirContents[i]});
//
//
//                if(fileExt == null)
//                    continue;
//
//                cacheEntryUri = UMFileUtil.removeExtension(dirContents[i]) + ".opds";
//
//                for(j = 0; j < supportedTypePlugins.length; j++) {
//                    if(UMUtil.getIndexInArray(fileExt, supportedTypePlugins[j].getFileExtensions()) != -1) {
//                        entryResult = supportedTypePlugins[j].getEntry(fileUri, cacheDirUri);
//
//                        if(entryResult == null || entryResult.getFeed().size() == 0)
//                            continue;//see if another plugin can handle it
//
//                        fileFeed = entryResult.getFeed();
//
//                        linkFeedUri = UMFileUtil.joinPaths(new String[]{directoryUri, dirContents[i]
//                                + ".links.opds"});
//                        linkFeed = null;
//
//                        if(impl.fileExists(linkFeedUri)) {
//                            linkFeed = new UstadJSOPDSFeed();
//                            try {
//                                linkFeedIn = impl.openFileInputStream(linkFeedUri);
//                                linkFeed.loadFromXpp(impl.newPullParser(linkFeedIn), null);
//                            }catch(XmlPullParserException x) {
//                                UstadMobileSystemImpl.l(UMLog.ERROR, 688, linkFeedUri, x);
//                            }catch(IOException e) {
//                                UstadMobileSystemImpl.l(UMLog.ERROR, 688, linkFeedUri, e);
//                            }finally {
//                                UMIOUtils.closeInputStream(linkFeedIn);
//                            }
//                        }
//
//                        for(k = 0; k < fileFeed.size(); k++) {
//                            entry =new UstadJSOPDSEntry(feed, fileFeed.getEntry(k));
//                            containerLinkHref = generateLink(fileUri, acquisitionLinkHrefPrefix,
//                                    null, entry.getItemId(), linkHrefMode);
//
//                            acquisitionLink = (UmOpdsLink)entry.getAcquisitionLinks().elementAt(0);
//                            acquisitionLink.setHref(containerLinkHref);
////                            acquisitionLink[UstadJSOPDSItem.ATTR_HREF] = containerLinkHref;
//
//                            if(entryResult.getThumbnailMimeType() != null) {
//                                try {
//                                    thumbnailData = entryResult.getThumbnail();
//                                    if(thumbnailData == null)
//                                        throw new IOException("Thumbnail file not found");
//
//                                    String extension = UstadMobileSystemImpl.getInstance()
//                                            .getExtensionFromMimeType(entryResult.getThumbnailMimeType());
//
//                                    String thumbnailFilename = UMFileUtil.removeExtension(
//                                            UMFileUtil.getFilename(fileUri))  + "-tmb." + extension;
//                                    String thumbnailAbsolutePath = UMFileUtil.joinPaths(
//                                            new String[]{cacheDirUri, thumbnailFilename});
//                                    thumbnailOut = impl.openFileOutputStream(thumbnailAbsolutePath, 0);
//                                    UMIOUtils.readFully(thumbnailData, thumbnailOut, 8 * 1024);
//
//                                    entry.addLink(UstadJSOPDSItem.LINK_REL_THUMBNAIL,
//                                            entryResult.getThumbnailMimeType(),
//                                            generateLink(thumbnailAbsolutePath,
//                                                    entryThumbnailLinkHrefPrefix,
//                                                    null, entry.getItemId(),
//                                                    thumbnailHrefMode));
//
//                                }catch(IOException e) {
//                                    UstadMobileSystemImpl.l(UMLog.ERROR, 688, null, e);
//                                }finally {
//                                    UMIOUtils.closeInputStream(thumbnailData);
//                                    UMIOUtils.closeOutputStream(thumbnailOut);
//                                    thumbnailData = null;
//                                    thumbnailOut = null;
//                                }
//                            }
//
//                            if(linkFeed != null && linkFeed.getEntryById(entry.getItemId()) != null) {
//                                Vector allLinks = linkFeed.getEntryById(entry.getItemId()).getLinks();
//                                for(int l = 0; l < allLinks.size(); l++) {
//                                    entry.addLink((String[])allLinks.elementAt(l));
//                                }
//                            }
//
//                            feed.addEntry(entry);
//                            if(callback != null)
//                                callback.onEntryLoaded(feed, feed.size(), entry);
//
//                            CatalogEntryInfo thisEntryInfo = CatalogPresenter.getEntryInfo(
//                                    entry.getItemId(), resourceMode, context);
//                            if(thisEntryInfo == null) {
//                                impl.l(UMLog.VERBOSE, 409, dirContents[i]);
//                                thisEntryInfo = new CatalogEntryInfo();
//                                thisEntryInfo.acquisitionStatus = STATUS_ACQUIRED;
//                                thisEntryInfo.fileURI = fileUri;
//                                thisEntryInfo.mimeType = entry.getFirstAcquisitionLink(null)
//                                        .getMimeType();
//
//                                thisEntryInfo.srcURLs = new String[] { dirContents[i] };
//
//
//                                CatalogPresenter.setEntryInfo(entry.getItemId(), thisEntryInfo,
//                                        resourceMode, context);
//                                if(impl.getNetworkManager() != null)
//                                    impl.getNetworkManager().handleEntryStatusChangeDiscovered(entry.getItemId(),
//                                            thisEntryInfo.acquisitionStatus);
//                            }
//
//                            if(thisEntryInfo.acquisitionStatus != STATUS_ACQUIRED) {
//                                thisEntryInfo.acquisitionStatus = STATUS_ACQUIRED;
//                                CatalogPresenter.setEntryInfo(entry.getItemId(), thisEntryInfo,
//                                        resourceMode, context);
//                            }
//                        }
//                    }
//                }
//            }
//
//        }catch(IOException e) {
//            UstadMobileSystemImpl.l(UMLog.ERROR, 673, directoryUri, e);
//        }


        return feed;
    }

    private String generateLink(String absoluteFilePath, String idPrefix, String postfix, String entryId,
                              int linkMode) {
        String link;

        switch(linkMode) {
            case LINK_HREF_MODE_ID:
                link = UMFileUtil.joinPaths(new String[]{
                        idPrefix, entryId});
                break;

            case LINK_HREF_MODE_RELATIVE:
                link = UMFileUtil.getFilename(absoluteFilePath);
                break;

            default:
                link = "file://" + absoluteFilePath;
                break;
        }

        if(postfix != null)
            link += postfix;

        return link;
    }

    /**
     * When generating the output OPDS the acquisition link can be an absolute file path (default),
     * a relative file path, or it can be a set prefixed path followed by the ID (e.g. for use with
     * the HTTP catalog server where entries are retrieved using /catalog/entry/entry-id
     *
     * @see #LINK_HREF_MODE_ABSOLUTE
     * @see #LINK_HREF_MODE_RELATIVE
     * @see #LINK_HREF_MODE_ID
     *
     * @return the current link href mode
     */
    public int getLinkHrefMode() {
        return linkHrefMode;
    }

    /**
     * When generating the output OPDS the acquisition link can be an absolute file path (default),
     * a relative file path, or it can be a set prefixed path followed by the ID (e.g. for use with
     * the HTTP catalog server where entries are retrieved using /catalog/entry/entry-id
     *
     *
     * @see #LINK_HREF_MODE_ABSOLUTE
     * @see #LINK_HREF_MODE_RELATIVE
     * @see #LINK_HREF_MODE_ID
     *
     * @param linkHrefMode the mode to use for any future calls to scanDirectory
     */
    public void setLinkHrefMode(int linkHrefMode) {
        this.linkHrefMode = linkHrefMode;
    }

    /**
     * Same as linkHrefMode - but for the thumbnail link.
     *
     * @see #LINK_HREF_MODE_ABSOLUTE
     * @see #LINK_HREF_MODE_RELATIVE
     * @see #LINK_HREF_MODE_ID
     *
     * @return the mode to use for generating thumbnail links
     */
    public int getThumbnailHrefMode() {
        return thumbnailHrefMode;
    }

    /**
     * Same as lnikHrefMode - but for the thumbnail link
     *
     * @see #LINK_HREF_MODE_ABSOLUTE
     * @see #LINK_HREF_MODE_RELATIVE
     * @see #LINK_HREF_MODE_ID
     *
     * @param thumbnailHrefMode the mode to use for generating thumbnail links
     */
    public void setThumbnailHrefMode(int thumbnailHrefMode) {
        this.thumbnailHrefMode = thumbnailHrefMode;
    }

    /**
     * If the link mode for thumbnails is set to LINK_HREF_MODE_ID this will be the prefix
     * to be added. Same as get/set acquisitionLinkHrefPrefix but for the thumbnail link
     *
     * @return The prefix to be used for thumbnail links
     */
    public String getEntryThumbnailLinkHrefPrefix() {
        return entryThumbnailLinkHrefPrefix;
    }

    /**
     * If the link mode for thumbnails is set to LINK_HREF_MODE_ID this will be the prefix
     * to be added. Same as get/set acquisitionLinkHrefPrefix but for the thumbnail link
     *
     * @param entryThumbnailLinkHrefPrefix The prefix to be used for thumbnail links
     */
    public void setEntryThumbnailLinkHrefPrefix(String entryThumbnailLinkHrefPrefix) {
        this.entryThumbnailLinkHrefPrefix = entryThumbnailLinkHrefPrefix;
    }

    /**
     * If the link mode is set to LINK_HREF_MODE_ID then this is the prefix that will be given before
     * the id e.g if it is set to /catalog/entry/ and a given entry has the id 12345 the resulting
     * acquisition link would be /catalog/entry/12345
     *
     * @return The prefix if using LINK_HREF_MODE_ID
     */
    public String getAcquisitionLinkHrefPrefix() {
        return acquisitionLinkHrefPrefix;
    }

    /**
     * If the link mode is set to LINK_HREF_MODE_ID then this is the prefix that will be given before
     * the id e.g if it is set to /catalog/entry/ and a given entry has the id 12345 the resulting
     * acquisition link would be /catalog/entry/12345
     *
     * @param acquisitionLinkHrefPrefix the prefix to use if using LINK_HREF_MODE_ID
     */
    public void setAcquisitionLinkHrefPrefix(String acquisitionLinkHrefPrefix) {
        this.acquisitionLinkHrefPrefix = acquisitionLinkHrefPrefix;
    }
}
