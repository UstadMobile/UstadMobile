package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.view.XapiPackageContentView;

import java.util.Arrays;
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
        return XapiPackageContentView.VIEW_NAME;
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
