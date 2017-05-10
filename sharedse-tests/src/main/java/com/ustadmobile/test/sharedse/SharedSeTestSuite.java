package com.ustadmobile.test.sharedse;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 5/10/17.
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
    BasicTest.class,
    BluetoothTest.class
})
public abstract class SharedSeTestSuite {
}
