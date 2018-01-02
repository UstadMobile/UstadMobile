package com.ustadmobile.test.core;

import com.ustadmobile.core.util.UMCalendarUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mike on 1/2/18.
 */

public class TestUmCalendarUtil {

    @Test
    public void testHttpDateParsing() {
        long date1 = UMCalendarUtil.parseHTTPDate("Sun, 06 Nov 1994 08:49:37 GMT");
        long date2 = UMCalendarUtil.parseHTTPDate("Sunday, 06-Nov-94 08:49:37 GMT");
        long date3 = UMCalendarUtil.parseHTTPDate("Sun Nov  6 08:49:37 1994 ");

        //The time in milliseconds between the date given and the date calculated
        // accurate to within one second
        Assert.assertTrue("Can parse date 1", Math.abs(784111777137L -date1) <= 1000);
        Assert.assertTrue("Can parse date 2", Math.abs(784111777137L -date2) <= 1000);
        Assert.assertTrue("Can parse date 3", Math.abs(784111777137L -date3) <= 1000);

        Assert.assertEquals("Can format HTTP Date", "Sun, 06 Nov 1994 08:49:37 GMT",
                UMCalendarUtil.makeHTTPDate(784111777137L));
    }
}
