package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.catalog.contenttype.EPUBTypePlugin;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.ocf.UstadOCFRootFile;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.opf.UstadJSOPFItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * Created by mike on 1/26/18.
 */

public class EpubTypePluginFs extends EPUBTypePlugin implements ContentTypePluginFs {

    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";

    @Override
    public List<OpdsEntryWithRelations> getEntries(File file, Object context) {
        InputStream ocfIn;
        List<OpdsEntryWithRelations> results = new ArrayList<>();
        try {
            ZipFile epubZip = ZipContentTypePluginHelper.openAndUnlock(file);

            UstadOCF ocf = new UstadOCF();
            FileHeader ocfEntry = epubZip.getFileHeader(OCF_CONTAINER_PATH);

            if(ocfEntry == null)
                return null;

            ocfIn = epubZip.getInputStream(ocfEntry);
            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(ocfIn,
                    "UTF-8");
            ocf.loadFromParser(xpp);
            ocfIn.close();

            for(UstadOCFRootFile root : ocf.rootFiles) {
                String url = UMFileUtil.PROTOCOL_FILE + file.getAbsolutePath() + "!" + root.fullPath;

                OpdsEntryWithRelations entry = DbManager.getInstance(context)
                        .getOpdsEntryWithRelationsDao().getEntryByUrlStatic(url);

                if(entry == null) {
                    entry = new OpdsEntryWithRelations();
                    entry.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
                    entry.setUrl(url);
                }

                FileHeader opfEntry = epubZip.getFileHeader(root.fullPath);
                ocfIn = epubZip.getInputStream(opfEntry);
                xpp = UstadMobileSystemImpl.getInstance().newPullParser(ocfIn, "UTF-8");
                UstadJSOPF opf = new UstadJSOPF();
                opf.loadFromOPF(xpp);
                ocfIn.close();

                entry.setTitle(opf.title);
                entry.setEntryId(opf.id);
                entry.setContent(opf.description);
                entry.setContentType(OpdsEntry.CONTENT_TYPE_TEXT);

                UstadJSOPFItem coverImageItem = opf.getCoverImage(null);
                if(coverImageItem != null) {
                    String coverImgHref = UMFileUtil.PROTOCOL_FILE + file.getAbsolutePath() + "!" +
                            UMFileUtil.resolveLink(root.fullPath, coverImageItem.href);
                    OpdsLink coverImgLink = new OpdsLink(entry.getUuid(),
                            coverImageItem.mimeType, coverImgHref, OpdsEntry.LINK_REL_THUMBNAIL);
                    if(entry.getLinks() == null)
                        entry.setLinks(new ArrayList<>());

                    entry.getLinks().add(coverImgLink);
                }

                results.add(entry);
            }
        }catch(IOException|XmlPullParserException|ZipException e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }
}
