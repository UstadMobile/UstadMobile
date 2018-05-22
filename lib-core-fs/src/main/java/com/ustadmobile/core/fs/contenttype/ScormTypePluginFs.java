package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.catalog.contenttype.ScormTypePlugin;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.scorm.ScormManifest;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.util.UmUuidUtil;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 2/3/18.
 */

public class ScormTypePluginFs extends ScormTypePlugin implements ContentTypePluginFs {

    @Override
    public List<OpdsEntryWithRelations> getEntries(File file, Object context) {
        InputStream manifestIn = null;
        OpdsEntryWithRelations entry = null;

        String url = UMFileUtil.PROTOCOL_FILE + file.getAbsolutePath();


        ZipFile zipFile = null;
        try {
            zipFile = ZipContentTypePluginHelper.openAndUnlock(file);
            FileHeader zipEntry = zipFile.getFileHeader("imsmanifest.xml");

            if(zipEntry == null)
                return null;

            entry = DbManager.getInstance(context)
                    .getOpdsEntryWithRelationsDao().getEntryByUrlStatic(url);

            if(entry == null){
                entry = new OpdsEntryWithRelations();
                entry.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
                entry.setUrl(url);
            }

            ScormManifest manifest = new ScormManifest();
            manifestIn = zipFile.getInputStream(zipEntry);
            manifest.loadFromInputStream(manifestIn);

            entry.setTitle(manifest.getDefaultOrganization().getTitle());
            entry.setEntryId(manifest.getIdentifier());
        }catch(IOException|XmlPullParserException|ZipException e) {
            e.printStackTrace();
        }finally{
            UMIOUtils.closeQuietly(manifestIn);
        }

        ArrayList<OpdsEntryWithRelations> result = new ArrayList<>();
        result.add(entry);
        return result;
    }

}
