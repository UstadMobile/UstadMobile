package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by mike on 12/25/17.
 */
public class TestEmbeddedHTTPD {

    static File epubToMount;

    static EmbeddedHTTPD httpd;

    static final String MOUNT_PATH = "thelittlechicks-test";

    static final String TEST_ENTRY_PATH = "EPUB/package.opf";

    static String mountedPath;

    @BeforeClass
    public static void mountZip() throws IOException {
        InputStream in = null;
        FileOutputStream fileOut = null;
        IOException ioe = null;
        try {
            epubToMount = File.createTempFile("TestEmbeddedHTTPD", ".epub");
            in = TestEmbeddedHTTPD.class.getResourceAsStream(
                    "/com/ustadmobile/port/sharedse/networkmanager/thelittlechicks.epub");
            fileOut=  new FileOutputStream(epubToMount);
            UMIOUtils.readFully(in, fileOut, 8*1024);

            httpd = new EmbeddedHTTPD(0, PlatformTestUtil.getTargetContext());
            httpd.start();
            mountedPath = httpd.mountZip(epubToMount.getAbsolutePath(), MOUNT_PATH);
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(in);
            UMIOUtils.closeOutputStream(fileOut);
        }

        UMIOUtils.throwIfNotNullIO(ioe);
    }

    @AfterClass
    public static void unmount() throws IOException {
        httpd.stop();
        httpd = null;
        epubToMount.delete();
    }


}
