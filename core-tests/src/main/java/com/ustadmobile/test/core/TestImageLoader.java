package com.ustadmobile.test.core;

import com.ustadmobile.core.controller.UstadBaseController;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by mike on 8/3/17.
 */

public class TestImageLoader {

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }


    private class TestController extends UstadBaseController{

        public TestController(Object context) {
            super(context);
        }

        @Override
        public void setUIStrings() {

        }
    }

    @Test
    public void testImageLoader() throws IOException{
//        final String[] loadedImage = new String[1];
//        final Object waitLock = new Object();
//        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//        ImageLoader.ImageLoadTarget testTarget = new ImageLoader.ImageLoadTarget() {
//            @Override
//            public void setImageFromFile(String filePath) {
//                loadedImage[0] = filePath;
//                synchronized (waitLock) {
//                    waitLock.notify();
//                }
//            }
//        };
//
//        Object context = PlatformTestUtil.getTargetContext();
//        TestController controller = new TestController(context);
//
//        String httpRoot = ResourcesHttpdTestServer.getHttpRoot();
//        String httpURL = UMFileUtil.joinPaths(new String[] {httpRoot,
//                "phonepic-smaller.png"});
//        ImageLoader.getInstance().loadImage(httpURL, testTarget, controller);
//        synchronized (waitLock){
//            try { waitLock.wait(15000); }
//            catch(InterruptedException e) {}
//        }
//        Assert.assertTrue("Downloaded image from file exists", impl.fileExists(loadedImage[0]));
    }

}
