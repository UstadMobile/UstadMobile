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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by mike on 9/13/17.
 *
 *
 */

public class XapiPackageTypePlugin extends ContentTypePlugin{

    private static final String[] MIME_TYPES = new String[] {"application/zip"};

    private static final String[] FILE_EXTENSIONS = new String[]{"zip"};

    //As per spec - there should be one and only one tincan.xml file
    protected static final String XML_FILE_NAME = "tincan.xml";

    @Override
    public String getViewName() {
        return XapiPackageView.VIEW_NAME;
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList(MIME_TYPES);
    }

    @Override
    public List<String> getFileExtensions() {
        return Arrays.asList(FILE_EXTENSIONS);
    }

}
