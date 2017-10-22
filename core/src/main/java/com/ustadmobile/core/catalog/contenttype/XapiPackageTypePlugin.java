package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.tincan.Activity;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.XapiPackageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * Created by mike on 9/13/17.
 *
 *
 */

public class XapiPackageTypePlugin extends ZippedContentTypePlugin{

    private static final String[] MIME_TYPES = new String[] {"application/zip"};

    private static final String[] FILE_EXTENSIONS = new String[]{"zip"};

    //As per spec - there should be one and only one tincan.xml file
    private static final String XML_FILE_NAME = "tincan.xml";

    @Override
    public String getViewName() {
        return XapiPackageView.VIEW_NAME;
    }

    @Override
    public String[] getMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public String[] getFileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public EntryResult getEntry(String fileUri, String cacheEntryFileUri) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        String containerFilename = UMFileUtil.getFilename(fileUri);
        String cacheFeedID = CatalogPresenter.sanitizeIDForFilename(fileUri);
        UstadJSOPDSFeed result = new UstadJSOPDSFeed(fileUri, containerFilename,
                cacheFeedID);

        ZipFileHandle zipHandle = null;
        ZipEntryHandle entryHandle;
        TinCanXML tinCanXML;
        InputStream tinCanXmlIn = null;
        UstadJSOPDSEntry tincanEntry = null;

        try {
            zipHandle = impl.openZip(fileUri);

            //find tincan.xml
            Enumeration entries = zipHandle.entries();

            while(entries.hasMoreElements()) {
                entryHandle = (ZipEntryHandle)entries.nextElement();
                if(entryHandle.getName().endsWith(XML_FILE_NAME)) {
                    try {
                        tinCanXmlIn = zipHandle.openInputStream(entryHandle.getName());
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        UMIOUtils.readFully(tinCanXmlIn, bout, 1024);
                        String tincanStr = new String(bout.toByteArray());
                        System.out.println(tincanStr);
                        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());

                        XmlPullParser xpp = impl.newPullParser(bin, "UTF-8");
                        tinCanXML = TinCanXML.loadFromXML(xpp);
                        Activity launchActivity = tinCanXML.getLaunchActivity();
                        if(launchActivity == null) {
                            return null;//we can only use tincan.xml files with a launch entry
                        }

                        tincanEntry = new UstadJSOPDSEntry(result,
                                launchActivity.getName(), launchActivity.getId(),
                                UstadJSOPDSEntry.LINK_ACQUIRE, MIME_TYPES[0], fileUri);
                        result.addEntry(tincanEntry);
                        break;
                    }catch(XmlPullParserException xe) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, 674, fileUri, xe);
                    }
                }
            }
        }catch(IOException ioe) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 675, fileUri, ioe);
        }finally {
            UMIOUtils.closeInputStream(tinCanXmlIn);
            UMIOUtils.closeZipFileHandle(zipHandle);
        }


        return tincanEntry != null ? new ZippedEntryResult(result, fileUri, null, null) : null;
    }
}
