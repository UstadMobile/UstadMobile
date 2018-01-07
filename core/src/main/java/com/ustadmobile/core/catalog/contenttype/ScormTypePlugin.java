package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.scorm.ScormManifest;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ScormPackageView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 1/6/18.
 */

public class ScormTypePlugin extends ZippedContentTypePlugin {

    public static final String[] MIME_TYPES = new String[]{"application/scorm+zip"};

    @Override
    public String getViewName() {
        return ScormPackageView.VIEW_NAME;
    }

    @Override
    public String[] getMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{"zip"};
    }

    @Override
    public EntryResult getEntry(String fileUri, String cacheEntryFileUri) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        InputStream manifestIn = null;
        UstadJSOPDSEntry entry = null;

        String containerFilename = UMFileUtil.getFilename(fileUri);
        String cacheFeedID = CatalogPresenter.sanitizeIDForFilename(fileUri);
        UstadJSOPDSFeed result = new UstadJSOPDSFeed(fileUri, containerFilename,
                cacheFeedID);
        try {
            ZipFileHandle zipHandle = impl.openZip(fileUri);
            ZipEntryHandle entryHandle = zipHandle.getEntry("imsmanifest.xml");
            if(entryHandle == null)
                return null;


            ScormManifest manifest = new ScormManifest();
            manifestIn = zipHandle.openInputStream("imsmanifest.xml");
            manifest.loadFromInputStream(manifestIn);

            entry = new UstadJSOPDSEntry(result, manifest.getDefaultOrganization().getTitle(),
                    manifest.getIdentifier(),
                    UstadJSOPDSEntry.LINK_ACQUIRE, MIME_TYPES[0], fileUri);
            result.addEntry(entry);

        }catch(IOException e) {
            e.printStackTrace();
        }catch(XmlPullParserException x) {
            x.printStackTrace();
        }catch(Exception e3) {
            e3.printStackTrace();
        }

        if(entry == null ){
            System.out.println("WTF");
        }

        return entry != null ? new ZippedEntryResult(result, fileUri, null, null) : null;
    }
}
