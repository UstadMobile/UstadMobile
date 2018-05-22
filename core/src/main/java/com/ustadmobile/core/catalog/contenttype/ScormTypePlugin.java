package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.view.ScormPackageView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 1/6/18.
 */

public class ScormTypePlugin extends ContentTypePlugin {

    public static final String[] MIME_TYPES = new String[]{"application/scorm+zip"};

    @Override
    public String getViewName() {
        return ScormPackageView.VIEW_NAME;
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList(MIME_TYPES);
    }

    @Override
    public List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"zip"});
    }

}
