package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.view.EpubContentView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 9/9/17.
 */

public class EPUBTypePlugin extends ContentTypePlugin {

    public static final String[] MIME_TYPES = new String[]{"application/epub+zip"};

    public static final String[] EXTENSIONS = new String[]{"epub"};

    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";

    @Override
    public String getViewName() {
        return EpubContentView.VIEW_NAME;
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList(MIME_TYPES);
    }

    @Override
    public List<String> getFileExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

}
