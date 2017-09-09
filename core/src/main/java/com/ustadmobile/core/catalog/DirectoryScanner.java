package com.ustadmobile.core.catalog;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUtil;

import java.io.IOException;
import java.util.Vector;

import static com.ustadmobile.core.controller.CatalogController.STATUS_ACQUIRED;

/**
 * Created by mike on 9/9/17.
 */

public class DirectoryScanner {

    /**
     * Flag for use with scanFiles: indicates that the feed acquisition links should be set using
     * with file URLs
     */
    public static final int LINK_HREF_MODE_FILE = 0;

    /**
     * Flag for use with scanFiles: indicates that the feed acquisition links should be set using
     * ids e.g. baseHref/containerId
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
     * @return
     */
    public UstadJSOPDSFeed scanDirectory(String directoryUri, String cacheDirUri, String title,
                                         String feedId, int resourceMode, Object context){
        String baseHref = linkHrefMode == LINK_HREF_MODE_ID ? hrefModeBaseHref : directoryUri;
        UstadJSOPDSFeed result = new UstadJSOPDSFeed(UMFileUtil.ensurePathHasPrefix(UMFileUtil.PROTOCOL_FILE, baseHref),
            title, feedId);
        try {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            Class[] supportedContentTypes = impl.getSupportedContentTypePlugins();


            ContentTypePlugin[] supportedTypePlugins = new ContentTypePlugin[supportedContentTypes.length];
            for(int i = 0; i < supportedTypePlugins.length; i++) {
                supportedTypePlugins[i] = (ContentTypePlugin)supportedContentTypes[i].newInstance();
            }

            String[] dirContents = impl.listDirectory(directoryUri);

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
                            entry =new UstadJSOPDSEntry(result, fileFeed.entries[k]);

                            //If this is a catalog being made to serve over HTTP : replace acquisition HREF with a base path followed by the ID
                            if(linkHrefMode == LINK_HREF_MODE_ID) {
                                acquisitionLinks = entry.getAcquisitionLinks();
                                if(acquisitionLinks != null && acquisitionLinks.size() > 0) {
                                    String[] links = (String[])acquisitionLinks.elementAt(0);
                                    links[UstadJSOPDSItem.ATTR_HREF] = UMFileUtil.joinPaths(new String[]{
                                            hrefModeBaseHref, entry.id});
                                }
                            }

                            result.addEntry(entry);

                            CatalogEntryInfo thisEntryInfo = CatalogController.getEntryInfo(
                                    entry.id, resourceMode, context);
                            if(thisEntryInfo == null) {
                                impl.l(UMLog.VERBOSE, 409, dirContents[i]);
                                thisEntryInfo = new CatalogEntryInfo();
                                thisEntryInfo.acquisitionStatus = STATUS_ACQUIRED;
                                thisEntryInfo.fileURI = fileUri;
                                thisEntryInfo.mimeType = UstadJSOPDSItem.TYPE_EPUBCONTAINER;
                                thisEntryInfo.srcURLs = new String[] { dirContents[i] };
                                CatalogController.setEntryInfo(entry.id, thisEntryInfo,
                                        resourceMode, context);
                            }

                            if(thisEntryInfo.acquisitionStatus != STATUS_ACQUIRED) {
                                thisEntryInfo.acquisitionStatus = STATUS_ACQUIRED;
                                CatalogController.setEntryInfo(entry.id, thisEntryInfo,
                                        resourceMode, context);
                            }
                        }
                    }
                }
            }

        }catch(IOException e) {

        }catch(InstantiationException i) {

        }catch(IllegalAccessException a) {

        }


        return result;
    }


}
