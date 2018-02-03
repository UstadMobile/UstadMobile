package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.core.catalog.contenttype.XapiPackageTypePlugin;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.tincan.Activity;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by mike on 2/3/18.
 */

public class XapiPackageTypePluginFs extends XapiPackageTypePlugin implements ContentTypePluginFs {

    @Override
    public List<OpdsEntryWithRelations> getEntries(File file, Object context) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        ZipFile zipFile = null;
        ZipEntry zipEntry = null;

        TinCanXML tinCanXML;
        InputStream tinCanXmlIn = null;

        OpdsEntryWithRelations tincanEntry = null;

        try {
            zipFile = new ZipFile(file);

            //find tincan.xml
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()) {
                zipEntry = entries.nextElement();
                if(zipEntry.getName().endsWith(XML_FILE_NAME)) {
                    try {
                        tinCanXmlIn = zipFile.getInputStream(zipEntry);
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        UMIOUtils.readFully(tinCanXmlIn, bout, 1024);
                        String tincanStr = new String(bout.toByteArray());
                        System.out.println(tincanStr);
                        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());

                        XmlPullParser xpp = impl.newPullParser(bin, "UTF-8");
                        tinCanXML = TinCanXML.loadFromXML(xpp);
                        Activity launchActivity = tinCanXML.getLaunchActivity();
                        if(launchActivity == null) {
                            return null;//we can only use tincan.xml files with a launch entry
                        }

                        String url = UMFileUtil.PROTOCOL_FILE + file.getAbsolutePath();
                        tincanEntry = DbManager.getInstance(context)
                                .getOpdsEntryWithRelationsDao().getEntryByUrlStatic(url);
                        if(tincanEntry == null) {
                            tincanEntry = new OpdsEntryWithRelations();
                            tincanEntry.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
                            tincanEntry.setUrl(url);
                        }

                        tincanEntry.setTitle(launchActivity.getName());
                        tincanEntry.setEntryId(launchActivity.getId());
                        tincanEntry.setContent(launchActivity.getDesc());
                        tincanEntry.setContentType(OpdsEntry.CONTENT_TYPE_TEXT);

                        break;
                    }catch(XmlPullParserException xe) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, 674, file.getAbsolutePath(), xe);
                    }
                }
            }
        }catch(IOException ioe) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 675, file.getAbsolutePath(), ioe);
        }finally {
            UMIOUtils.closeInputStream(tinCanXmlIn);
            if(zipFile != null) {
                try { zipFile.close();}
                catch(IOException e) {}
            }
        }

        if(tincanEntry == null) {
            return null;
        }else{
            ArrayList<OpdsEntryWithRelations> result = new ArrayList<>();
            result.add(tincanEntry);
            return result;
        }
    }
}
