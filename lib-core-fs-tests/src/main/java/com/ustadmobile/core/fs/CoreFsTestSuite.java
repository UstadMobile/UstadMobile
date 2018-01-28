package com.ustadmobile.core.fs;

import com.ustadmobile.core.fs.db.TestOpdsRepository;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 12/26/17.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestHttpCache.class,
    TestOpdsRepository.class
})

public class CoreFsTestSuite {
}
