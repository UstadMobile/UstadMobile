package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.catalog.contenttype.ScormTypePlugin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.io.File;
import java.util.List;

/**
 * Created by mike on 2/3/18.
 */

public class ScormTypePluginFs extends ScormTypePlugin implements ContentTypePluginFs {
    @Override
    public List<OpdsEntryWithRelations> getEntries(File file, Object context) {
        return null;
    }

    //    @Override
//    public EntryResult getEntry(String fileUri, String cacheEntryFileUri) {
//        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//
//        InputStream manifestIn = null;
//        UstadJSOPDSEntry entry = null;
//
//        String containerFilename = UMFileUtil.getFilename(fileUri);
//        String cacheFeedID = CatalogPresenter.sanitizeIDForFilename(fileUri);
//        UstadJSOPDSFeed result = new UstadJSOPDSFeed(fileUri, containerFilename,
//                cacheFeedID);
//        try {
//            ZipFileHandle zipHandle = impl.openZip(fileUri);
//            ZipEntryHandle entryHandle = zipHandle.getEntry("imsmanifest.xml");
//            if(entryHandle == null)
//                return null;
//
//
//            ScormManifest manifest = new ScormManifest();
//            manifestIn = zipHandle.openInputStream("imsmanifest.xml");
//            manifest.loadFromInputStream(manifestIn);
//
//            entry = new UstadJSOPDSEntry(result, manifest.getDefaultOrganization().getTitle(),
//                    manifest.getIdentifier(),
//                    UstadJSOPDSEntry.LINK_ACQUIRE, MIME_TYPES[0], fileUri);
//            result.addEntry(entry);
//
//        }catch(IOException e) {
//            e.printStackTrace();
//        }catch(XmlPullParserException x) {
//            x.printStackTrace();
//        }catch(Exception e3) {
//            e3.printStackTrace();
//        }
//
//        if(entry == null ){
//            System.out.println("WTF");
//        }
//
//        return entry != null ? new ZippedEntryResult(result, fileUri, null, null) : null;
//    }
}
