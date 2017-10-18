package com.ustadmobile.ustadmobile.port.sharedse.utillib;

import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Updates the acquisition links of an OPDS file and sets the length attribute on the acquisition
 * links based on the actual file size.
 *
 * Usage: OpdsUpdater /path/to/file.opds
 */
public class OpdsUpdater {

    private static final int ARG_OPDS_FILE_INDEX = 0;

    private static final int ARG_BASE_PATH = 1;

    private static final int ARG_OPDS_FILE_OUT = 2;

    public static void main(String[] args) {
        System.out.println("Updating opds in directory: ");
        UstadJSOPDSFeed feed = new UstadJSOPDSFeed();
        XmlPullParser parser = new KXmlParser();
        FileInputStream fin;
        FileOutputStream fout;
        File entryFile;

        try {
            fin = new FileInputStream(args[ARG_OPDS_FILE_INDEX]);
            parser.setInput(fin, "UTF-8");
            feed.loadFromXpp(parser, null);
            fin.close();

            System.out.println("Loaded feed");
            String[] entryAcquisitionLinks;
            for(int i = 0; i < feed.size(); i++) {
                entryAcquisitionLinks = feed.getEntry(i).getFirstAcquisitionLink(null);
                entryFile = new File(args[ARG_BASE_PATH], entryAcquisitionLinks[UstadJSOPDSItem.ATTR_HREF]);
                entryAcquisitionLinks[UstadJSOPDSItem.ATTR_LENGTH] = String.valueOf(entryFile.length());
            }

            fout = new FileOutputStream(args[ARG_OPDS_FILE_OUT]);
            XmlSerializer serializer = new KXmlSerializer();
            serializer.setOutput(fout, "UTF-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);


            feed.serialize(serializer);
            fout.flush();
            fout.close();
        }catch (IOException|XmlPullParserException e) {
            e.printStackTrace();
        }


    }

}
