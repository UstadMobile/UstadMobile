package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.opf.UstadJSOPFItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.ContainerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mike on 9/9/17.
 */

public class EPUBTypePlugin extends ZippedContentTypePlugin {

    private static final String[] MIME_TYPES = new String[]{"application/epub+zip"};

    private static final String[] EXTENSIONS = new String[]{"epub"};

    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";

    @Override
    public String getViewName() {
        return ContainerView.VIEW_NAME;
    }

    @Override
    public String[] getMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public String[] getFileExtensions() {
        return EXTENSIONS;
    }

    @Override
    public EntryResult getEntry(String fileUri, String cacheEntryFileUri) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.l(UMLog.VERBOSE, 437, fileUri);

        String containerFilename = UMFileUtil.getFilename(fileUri);
        String cacheFeedID = CatalogPresenter.sanitizeIDForFilename(fileUri);
        UstadJSOPDSFeed result = new UstadJSOPDSFeed(fileUri, containerFilename,
                cacheFeedID);

        String absfileUri = UMFileUtil.ensurePathHasPrefix("file://", fileUri);

        ZipFileHandle zipHandle = null;
        InputStream zIs = null;
        UstadOCF ocf;
        UstadJSOPF opf;
        UstadJSOPDSEntry epubEntry;

        String thumbnailPathInZip = null;
        String thumbnailMimeType = null;

        long fileLength;

        int j;

        try {
            zipHandle = impl.openZip(fileUri);
            fileLength = impl.fileSize(fileUri);
            zIs = zipHandle.openInputStream(OCF_CONTAINER_PATH);

            if(zIs != null) {
                ocf = UstadOCF.loadFromXML(impl.newPullParser(zIs));
                UMIOUtils.closeInputStream(zIs);

                for(j = 0; j < ocf.rootFiles.length; j++) {
                    zIs = zipHandle.openInputStream(ocf.rootFiles[j].fullPath);
                    opf = new UstadJSOPF();
                    opf.loadFromOPF(impl.newPullParser(zIs),
                            UstadJSOPF.PARSE_METADATA | UstadJSOPF.PARSE_MANIFEST);
                    UMIOUtils.closeInputStream(zIs);
                    zIs = null;

                    epubEntry =new UstadJSOPDSEntry(result,opf,
                            UstadJSOPDSItem.TYPE_EPUBCONTAINER, absfileUri);
                    String[] acquireLink = epubEntry.getLink(0);
                    acquireLink[UstadJSOPDSEntry.ATTR_LENGTH] = String.valueOf(fileLength);
                    epubEntry.setLinkAt(acquireLink, 0);

                    UstadJSOPFItem coverItem = opf.getCoverImage(null);

                    if(coverItem != null) {
                        thumbnailPathInZip = UMFileUtil.resolveLink(ocf.rootFiles[j].fullPath,
                                coverItem.href);
                        thumbnailMimeType = coverItem.mimeType;
                    }


                    result.addEntry(epubEntry);
                }
            }else {
                result = null;
            }


        }catch(Exception e) {
            impl.l(UMLog.ERROR, 142, fileUri, e);
        }finally {
            UMIOUtils.closeInputStream(zIs);
            UMIOUtils.closeZipFileHandle(zipHandle);
        }


        if(result != null) {
            return new ZippedEntryResult(result, fileUri, thumbnailPathInZip, thumbnailMimeType);
        }else {
            return null;
        }
    }
}
