package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.model.AttendanceClassStudent;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mike on 5/10/17.
 */

public class BasicTest {

    @Test
    public void testAttendanceStudent() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Assert.assertNotNull(impl);
        AttendanceClassStudent student = new AttendanceClassStudent("bob", "Bob", "Bob Jones");
        Assert.assertEquals("Bob", student.name);
    }


}
