package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.opf.UstadJSOPFItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 9/9/17.
 */

public class EPUBTypePlugin extends ContentTypePlugin {

    private static final String[] MIME_TYPES = new String[]{"application/epub+zip"};

    private static final String[] EXTENSIONS = new String[]{"epub"};

    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";

    @Override
    public String getViewName() {
        return ContainerView.VIEW_NAME;
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
