package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.port.android.view.BasePointActivity2;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SmallEspressoTest {

    @Rule
    public ActivityTestRule<BasePointActivity2> mActivityRule =
            new ActivityTestRule<>(BasePointActivity2.class);

    @Test
    public void testMe(){

        Assert.assertTrue(true);

    }
}
