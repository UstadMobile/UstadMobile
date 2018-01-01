package com.ustadmobile.ustadmobile.port.sharedse.utillib;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMIOUtils;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by mike on 10/18/17.
 */

public class TmpSlicer {

    public static final String[] LANGUAGES = new String[]{"en", "fa", "ps"};


    public static final String[] TITLES = new String[]{"Mobile Soft Skills Training","",""};

    public static final String[] MODULES = new String[]{
            "1-cvwriting", "2-coverletter", "3-jobsearch", "4-interview", "5-communication",
            "6-ethics", "7-timemanagement", "8-reportwriting", "9-entrepreneurship"
    };


    public static void main(String[] args) throws Exception{
        File modulesDir = new File(args[0]);
        File outDir = new File(args[1]);

        File[] moduleDirs = new File[MODULES.length];
        for(int i = 0; i < moduleDirs.length; i++) {
            moduleDirs[i] = new File(modulesDir, MODULES[i]);
        }

        UstadJSOPDSFeed[] feeds = new UstadJSOPDSFeed[3];
        for(int i = 0; i < feeds.length; i++) {
            feeds[i] = new UstadJSOPDSFeed("http://www.ustadmobile.com/files/s4s/index."
                + LANGUAGES[i] + ".opds");
            feeds[i].id = "com.ustadmobile.msst." + LANGUAGES[i];
            feeds[i].title = TITLES[i];
            feeds[i].setLanguage(LANGUAGES[i]);

            //now add links to other languages
            for(int j = 0; j < LANGUAGES.length; j++) {
                if(j == i)
                    continue;

                String[] altCatalogLink = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                altCatalogLink[UstadJSOPDSItem.ATTR_HREFLANG] = LANGUAGES[j];
                altCatalogLink[UstadJSOPDSItem.ATTR_HREF] = "../" + LANGUAGES[j] + "/index.opds";
                altCatalogLink[UstadJSOPDSItem.ATTR_MIMETYPE] = UstadJSOPDSItem.TYPE_ACQUISITIONFEED;
                altCatalogLink[UstadJSOPDSItem.ATTR_REL] = UstadJSOPDSItem.LINK_REL_ALTERNATE;
                feeds[i].addLink(altCatalogLink);
            }

        }

        for(int i = 0; i < MODULES.length; i++) {
            for(int j = 0; j < LANGUAGES.length; j++) {
                File moduleOutDir = new File(outDir, LANGUAGES[j]);
                if(!moduleOutDir.isDirectory())
                    moduleOutDir.mkdirs();

                InputStream opfIn = null;
                OutputStream entryOut = null;
                File opfFile = new File(moduleDirs[i], LANGUAGES[j] + "/EPUB/package.opf");
                File entryFile = new File(moduleOutDir, MODULES[i] + "-" + LANGUAGES[j]
                        + ".epub.entry.opds");

                //zip the module itself into an EPUB
                File epubFile = new File(moduleOutDir, MODULES[i] + "-" + LANGUAGES[j] + ".epub");
                ZipOutputStream zipOutputStream = null;
                File epubBaseDir = new File(moduleDirs[i], LANGUAGES[j]);
                try {
                    zipOutputStream = new ZipOutputStream(new FileOutputStream(epubFile));
                    zipDirRecursive(zipOutputStream, new File(epubBaseDir, "EPUB"), "");
                    zipDirRecursive(zipOutputStream, new File(epubBaseDir, "META-INF"), "");
                }catch(IOException e) {
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeOutputStream(zipOutputStream);
                }

                try {
                    opfIn = new FileInputStream(opfFile);
                    XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(opfIn);
                    UstadJSOPF opfObj = new UstadJSOPF();
                    opfObj.loadFromOPF(xpp);
                    System.out.println("loaded: " + opfObj.title);

                    UstadJSOPDSEntry linkEntry = new UstadJSOPDSEntry(feeds[j]);
                    linkEntry.id = opfObj.id;
                    linkEntry.title = opfObj.title;
                    if(opfObj.description != null) {
                        linkEntry.setContent(opfObj.description);
                    }

                    //add language links
                    for(int k = 0; k < LANGUAGES.length; k++) {
                        if(k == j)
                            continue;

                        String[] altLangLink = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                        altLangLink[UstadJSOPDSItem.ATTR_HREF] = "../" + LANGUAGES[k] + "/" +
                                moduleDirs[i].getName() + "-" + LANGUAGES[k] + ".epub.entry.opds";
                        altLangLink[UstadJSOPDSItem.ATTR_REL] = "alternate";
                        altLangLink[UstadJSOPDSItem.ATTR_HREFLANG] = LANGUAGES[k];
                        altLangLink[UstadJSOPDSItem.ATTR_MIMETYPE] = UstadJSOPDSItem.TYPE_ENTRY_OPDS;
                        linkEntry.addLink(altLangLink);
                    }

                    int alsoFrom = 0;
                    int alsoTo = MODULES.length;
                    if(i < 4) {
                        alsoTo = 4;
                    }else {
                        alsoFrom = 4;
                    }

                    //add see also
                    for(int k = alsoFrom; k < alsoTo; k++) {
                        if(k == i)
                            continue;

                        String[] seeAlsoLinks = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                        seeAlsoLinks[UstadJSOPDSItem.ATTR_HREF] = MODULES[k] + "-" + LANGUAGES[j]
                                + ".epub.entry.opds";
                        seeAlsoLinks[UstadJSOPDSItem.ATTR_MIMETYPE] = UstadJSOPDSItem.TYPE_ENTRY_OPDS;
                        seeAlsoLinks[UstadJSOPDSItem.ATTR_REL] = "related";

                        File seeAlsoOpfFile = new File(modulesDir, MODULES[k] + "/" + LANGUAGES[j] +
                                "/EPUB/package.opf");
                        FileInputStream fin = new FileInputStream(seeAlsoOpfFile);
                        XmlPullParser seeAlsoXpp = UstadMobileSystemImpl.getInstance().newPullParser(fin);
                        UstadJSOPF seeAlsoOpf = new UstadJSOPF();
                        seeAlsoOpf.loadFromOPF(seeAlsoXpp);
                        seeAlsoLinks[UstadJSOPDSItem.ATTR_TITLE] = seeAlsoOpf.title;
                        linkEntry.addLink(seeAlsoLinks);
                    }

                    String[] coverImgLink = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                    coverImgLink[UstadJSOPDSItem.ATTR_HREF] = "../opds-img/" + MODULES[i] +"-cover.jpg";
                    coverImgLink[UstadJSOPDSItem.ATTR_MIMETYPE] = "image/jpg";
                    coverImgLink[UstadJSOPDSItem.ATTR_REL] = "http://www.ustadmobile.com/ns/opds/cover-image";
                    linkEntry.addLink(coverImgLink);

                    String[] thumbnailLink = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                    thumbnailLink[UstadJSOPDSItem.ATTR_HREF] = "../opds-img/" + MODULES[i] + "-thumb.png";
                    thumbnailLink[UstadJSOPDSItem.ATTR_MIMETYPE] = "image/png";
                    thumbnailLink[UstadJSOPDSItem.ATTR_REL] = UstadJSOPDSItem.LINK_REL_THUMBNAIL;
                    linkEntry.addLink(thumbnailLink);

                    //Add the acquire link
                    String[] acquireLink = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                    acquireLink[UstadJSOPDSItem.ATTR_REL] = UstadJSOPDSItem.LINK_ACQUIRE;
                    acquireLink[UstadJSOPDSItem.ATTR_HREF] = epubFile.getName();
                    acquireLink[UstadJSOPDSItem.ATTR_MIMETYPE] = UstadJSOPDSItem.TYPE_EPUBCONTAINER;
                    acquireLink[UstadJSOPDSItem.ATTR_LENGTH] = String.valueOf(epubFile.length());
                    linkEntry.addLink(acquireLink);

                    linkEntry.setLanguage(LANGUAGES[j]);

                    entryOut = new FileOutputStream(entryFile);
                    entryOut.write(linkEntry.serializeToString(false, true).getBytes("UTF-8"));
                    entryOut.flush();

                    //now add it's own alternate link
                    String[] feedAltLink = new String[UstadJSOPDSItem.LINK_ATTRS_END];
                    feedAltLink[UstadJSOPDSItem.ATTR_REL] = UstadJSOPDSItem.LINK_REL_ALTERNATE;
                    feedAltLink[UstadJSOPDSItem.ATTR_HREF] = entryFile.getName();
                    feedAltLink[UstadJSOPDSItem.ATTR_MIMETYPE] = UstadJSOPDSItem.TYPE_ENTRY_OPDS;
                    feedAltLink[UstadJSOPDSItem.ATTR_HREFLANG] = LANGUAGES[j];
                    linkEntry.addLink(feedAltLink);

                    feeds[j].addEntry(linkEntry);
                }catch(IOException e) {
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeInputStream(opfIn);
                    UMIOUtils.closeOutputStream(entryOut);
                }
            }
        }

        for(int i = 0; i < LANGUAGES.length; i++) {
            File outIndexOpds = new File(outDir, LANGUAGES[i]+"/index.opds");
            FileOutputStream fout;
            try {
                fout = new FileOutputStream(outIndexOpds);
                fout.write(feeds[i].serializeToString(false, true).getBytes("UTF-8"));
                fout.flush();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void zipDirRecursive(ZipOutputStream zipOut, File dir, String prefix) throws IOException{
        File[] dirContents = dir.listFiles();
        for(int i = 0; i < dirContents.length; i++) {
            ZipEntry entry = new ZipEntry(prefix + dir.getName() + "/" + dirContents[i].getName());
            if(dirContents[i].isDirectory()){
                zipOut.putNextEntry(entry);
            }else{
                zipOut.putNextEntry(entry);
                FileInputStream fin = new FileInputStream(dirContents[i]);
                UMIOUtils.readFully(fin, zipOut, 1024*8);
                zipOut.closeEntry();
            }
        }
    }

}
