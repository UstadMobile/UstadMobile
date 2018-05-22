package com.ustadmobile.test.core.view;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.test.core.impl.AsyncTestHelper;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mike on 12/25/17.
 */
public abstract class TestXapiPackageView {

    private static final int LOAD_TIMEOUT = 10000;

    //Must be overriden by the implementation test to create the required view.
    public abstract XapiPackageView getView();

//    @Test
//    public void testPackageLoaded() {
//        final AsyncTestHelper helper = new AsyncTestHelper(this);
//
//        final Object lockObject = new Object();
//
//        final UmCallback loadCallback = new UmCallback() {
//            @Override
//            public void onSuccess(Object result) {
//                helper.runTests();
//            }
//
//            @Override
//            public void onFailure(Throwable exception) {
//                synchronized (lockObject) {
//                    lockObject.notifyAll();
//                }
//            }
//        };
//
//        final XapiPackageView view = getView();
//
//        helper.setTestsRunnable(new Runnable() {
//            @Override
//            public void run() {
//                Assert.assertNotNull("Presenter loaded", view.getPresenter());
//                Assert.assertNotNull("Loaded TinCan XML object",
//                        view.getPresenter().getTinCanXml());
//            }
//        });
//        view.getPresenter().setOnLoadListener(loadCallback);
//
//        if(!view.getPresenter().isLoadCompleted()) {
//            helper.delayTestFinish(LOAD_TIMEOUT);
//        }else{
//            helper.runTests();
//        }
//
//        helper.waitForTest();
//    }

}
