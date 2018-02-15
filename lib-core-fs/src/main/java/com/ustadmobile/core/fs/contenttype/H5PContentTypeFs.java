package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.catalog.contenttype.H5PContentType;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by mike on 2/15/18.
 */

public class H5PContentTypeFs extends H5PContentType implements ContentTypePluginFs {

    @Override
    public List<OpdsEntryWithRelations> getEntries(File file, Object context) {
        ZipFile zipFile = null;
        InputStream entryIn = null;
        OpdsEntryWithRelations entry = null;

        try {
            zipFile = new ZipFile(file);
            ZipEntry h5pJsonEntry = zipFile.getEntry("h5p.json");
            if(h5pJsonEntry == null)
                return null;//nothing really here

            entryIn = zipFile.getInputStream(h5pJsonEntry);
            JSONObject h5pJsonObj = new JSONObject(UMIOUtils.readStreamToString(entryIn));

            String fileUri = file.toURI().toString();
            entry = DbManager.getInstance(context)
                    .getOpdsEntryWithRelationsDao().getEntryByUrlStatic(fileUri);
            if(entry == null) {
                entry = new OpdsEntryWithRelations();
                entry.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
                entry.setUrl(fileUri);
            }

            entry.setTitle(h5pJsonObj.getString("title"));

            //This is not an ideal solution
            entry.setEntryId(fileUri);
        }catch(IOException e) {
            e.printStackTrace();
        }finally{
            UMIOUtils.closeInputStream(entryIn);
            if(zipFile != null) {
                try { zipFile.close(); }
                catch(IOException e) {}
            }
        }

        return entry != null ? Arrays.asList(entry) : null;
    }
}
