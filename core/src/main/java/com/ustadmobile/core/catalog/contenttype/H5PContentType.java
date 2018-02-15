package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.view.H5PContentView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 2/15/18.
 */

public class H5PContentType extends ContentTypePlugin{

    @Override
    public String getViewName() {
        return H5PContentView.VIEW_NAME;
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList("application/h5p+zip");
    }

    @Override
    public List<String> getFileExtensions() {
        return Arrays.asList("h5p");
    }
}
