package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.catalog.contenttype.H5PContentType;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ContentTypePluginFs implementation for H5P files.
 *
 * If the H5P file's h5p.json contains "entryId" - this will be used as the entry id attribute. This
 * is non-standard and not part of the H5P spec. It's thus difficult to match up what is described in
 * a feed and the actual file. If entryId is not in the h5p.json file, we use the file path.
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
            entry = UmAppDatabase.getInstance(context)
                    .getOpdsEntryWithRelationsDao().getEntryByUrlStatic(fileUri);
            if(entry == null) {
                entry = new OpdsEntryWithRelations();
                entry.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
                entry.setUrl(fileUri);
            }

            entry.setTitle(h5pJsonObj.getString("title"));
//          TODO: Unfortunately the SVG files in H5P appear to use features that are unsupported by
//              many SVG rendering libs, and display on Android and Ubuntu as a black square.

            OpdsLink thumbnailLink = new OpdsLink(entry.getUuid(),
                                    "image/png", "http://www.ustadmobile.com/files/h5plogo.png",
                    OpdsEntryWithRelations.LINK_REL_THUMBNAIL);
            if(entry.getLinks() == null)
                entry.setLinks(new ArrayList<>());

            entry.getLinks().add(thumbnailLink);

//            //Try to get the thumbnail
//            try {
//                String mainLib = h5pJsonObj.getString("mainLibrary");
//                JSONArray preloadedDeps  = h5pJsonObj.getJSONArray("preloadedDependencies");
//
//                for(int i = 0; i < preloadedDeps.length(); i++) {
//                    JSONObject depObj = preloadedDeps.getJSONObject(i);
//                    if(depObj.getString("machineName").equals(mainLib)) {
//                        String mainLibDir = mainLib + '-' + depObj.getString("majorVersion") +
//                                '.' + depObj.getString("minorVersion");
//                        ZipEntry iconEntry = zipFile.getEntry(mainLibDir+"/icon.svg");
//                        if(iconEntry != null){
//                            String coverHref =  UMFileUtil.PROTOCOL_FILE + file.getAbsolutePath() +
//                                    "!" +mainLibDir +"/icon.svg";
//
//                            OpdsLink thumbnailLink = new OpdsLink(entry.getUuid(),
//                                    "image/svg+xml", coverHref, OpdsEntryWithRelations.LINK_REL_THUMBNAIL);
//
//                            if(entry.getLinks() == null)
//                                entry.setLinks(new ArrayList<>());
//
//                            entry.getLinks().add(thumbnailLink);
//                            break;
//                        }
//                    }
//                }
//            }catch(Exception e) {
//                e.printStackTrace();
//            }

            //This is not an ideal solution

            String entryId = h5pJsonObj.optString("entryId", null);
            if(entryId != null) {
                entry.setEntryId(entryId);
            }else {
                entry.setEntryId(fileUri);
            }
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
