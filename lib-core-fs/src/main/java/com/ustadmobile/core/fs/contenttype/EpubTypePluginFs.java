package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.catalog.contenttype.EPUBTypePlugin;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.fs.db.NotifyUmObserer;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.ocf.UstadOCFRootFile;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by mike on 1/26/18.
 */

public class EpubTypePluginFs extends EPUBTypePlugin implements ContentTypePluginFs {

    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";

    @Override
    public List<OpdsEntryWithRelations> getEntries(String fileUri, Object context) {
        InputStream ocfIn;
        List<OpdsEntryWithRelations> results = new ArrayList<>();
        try {
            ZipFile epubZip = new ZipFile(fileUri);
            UstadOCF ocf = new UstadOCF();
            ZipEntry ocfEntry = epubZip.getEntry(OCF_CONTAINER_PATH);
            ocfIn = epubZip.getInputStream(ocfEntry);
            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(ocfIn,
                    "UTF-8");
            ocf.loadFromParser(xpp);
            ocfIn.close();

            for(UstadOCFRootFile root : ocf.rootFiles) {
                String url = fileUri + "!" + root.fullPath;

                OpdsEntryWithRelations entry = DbManager.getInstance(context)
                        .getOpdsEntryWithRelationsDao().getEntryByUrlStatic(url);

                if(entry == null) {
                    entry = new OpdsEntryWithRelations();
                    entry.setId(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
                    entry.setUrl(url);
                }

                ZipEntry opfEntry = epubZip.getEntry(root.fullPath);
                ocfIn = epubZip.getInputStream(opfEntry);
                xpp = UstadMobileSystemImpl.getInstance().newPullParser(ocfIn, "UTF-8");
                UstadJSOPF opf = new UstadJSOPF();
                opf.loadFromOPF(xpp);
                ocfIn.close();

                entry.setTitle(opf.title);
                entry.setItemId(opf.id);
                entry.setContent(opf.description);
                entry.setContentType(OpdsEntry.CONTENT_TYPE_TEXT);
                results.add(entry);
            }
        }catch(IOException|XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }
}
