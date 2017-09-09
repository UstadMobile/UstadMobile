package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.ContainerView;

import java.io.InputStream;

/**
 * Created by mike on 9/9/17.
 */

public class EPUBTypePlugin extends ContentTypePlugin{

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
    public UstadJSOPDSFeed getEntry(String fileUri, String cacheEntryFileUri) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.l(UMLog.VERBOSE, 437, fileUri);

        String containerFilename = UMFileUtil.getFilename(fileUri);
        String cacheFeedID = CatalogController.sanitizeIDForFilename(fileUri);
        UstadJSOPDSFeed result = new UstadJSOPDSFeed(fileUri, containerFilename,
                cacheFeedID);

        String absfileUri = UMFileUtil.ensurePathHasPrefix("file://", fileUri);

        //check and see if there is a given default thumbnail for this container
//        String[] imgExtensions = new String[]{"jpg", "png", "gif"};
//        String thumbURI = null;
//        String thumbMimeType = null;
//
//        for(int i = 0; i < imgExtensions.length; i++) {
//            try {
//                thumbURI = absfileUri + THUMBNAIL_POSTFIX + imgExtensions[i];
//                if(impl.fileExists(thumbURI)) {
//                    thumbMimeType = impl.getMimeTypeFromExtension(imgExtensions[i]);
//                    break;
//                }
//            }catch(Exception e) {
//                impl.l(UMLog.ERROR, 150, thumbURI, e);
//            }
//        }


        ZipFileHandle zipHandle = null;
        InputStream zIs = null;
        UstadOCF ocf;
        UstadJSOPF opf;
        UstadJSOPDSEntry epubEntry;
        int j;

        try {
            zipHandle = impl.openZip(fileUri);
            zIs = zipHandle.openInputStream(OCF_CONTAINER_PATH);

            if(zIs != null) {
                ocf = UstadOCF.loadFromXML(impl.newPullParser(zIs));
                UMIOUtils.closeInputStream(zIs);

                for(j = 0; j < ocf.rootFiles.length; j++) {
                    zIs = zipHandle.openInputStream(ocf.rootFiles[j].fullPath);
                    opf = UstadJSOPF.loadFromOPF(impl.newPullParser(zIs),
                            UstadJSOPF.PARSE_METADATA);
                    UMIOUtils.closeInputStream(zIs);
                    zIs = null;

                    epubEntry =new UstadJSOPDSEntry(result,opf,
                            UstadJSOPDSItem.TYPE_EPUBCONTAINER, absfileUri);
//                if(thumbMimeType != null) {//Thumb Mime type only set when we have a file
//                    epubEntry.addLink(UstadJSOPDSEntry.LINK_THUMBNAIL,
//                            thumbMimeType, thumbURI);
//                }

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


        return result;
    }
}
