package com.ustadmobile.test.port.android.view;

import android.arch.paging.DataSource;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.port.android.view.BasePointActivity2;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.matcher.ViewMatchers.withId;



/**
 * ClazzListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassListFragmentEspressoTest {


    private static final String TEST_CLASS_NAME = "ClassA";
    private static final float TEST_CLASS_PERCENTAGE = 0.42F;
    private static final float TEST_STUDENT_PERCENTAGE = 0.21F;
    private static final long TEST_USER_UID = 1L;

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public IntentsTestRule<BasePointActivity2> mActivityRule =
            new IntentsTestRule<>(BasePointActivity2.class, false, false);



    @Before
    public void beforeTest() throws Throwable{
        Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(UmAppDatabase.class.getName());
        UmAppDatabase.setInstance(null);

        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();

        Clazz clazz1 = new Clazz();
        clazz1.setClazzName(TEST_CLASS_NAME);
        clazz1.setAttendanceAverage(TEST_CLASS_PERCENTAGE);
        clazz1.setClazzUid(clazzDao.insert(clazz1));

        for (int i=0; i<2; i++){
            ClazzMember clazzMember = new ClazzMember();
            clazzMember.setAttendancePercentage(TEST_STUDENT_PERCENTAGE);
            clazzMember.setClazzMemberClazzUid(clazz1.getClazzUid());
            clazzMember.setRole(ClazzMember.ROLE_STUDENT);
            clazzMember.setClazzMemberPersonUid(TEST_USER_UID);

            clazzMemberDao.insert(clazzMember);
        }


        //Before here..
        mActivityRule.launchActivity(new Intent());

        mActivityRule.runOnUiThread(() ->
                ((AHBottomNavigation)mActivityRule.getActivity().findViewById(R.id.bottom_navigation)
                ).setCurrentItem(1));
    }

    @Test
    public void givenAppLoads_whenClassListFragmentStarts_shouldShowClassesCards() {
        Assert.assertTrue(true);

        String attendancePercentage = (TEST_STUDENT_PERCENTAGE * 100) + "%";

        onView(allOf(withId(R.id.item_clazzlist_clazz_title), withText(TEST_CLASS_NAME)))
                .check(matches(isDisplayed()));
        onView(allOf(hasSibling(withText(TEST_CLASS_NAME)),
                withId(R.id.item_clazzlist_attendance_percentage),
                withText(containsString(attendancePercentage)))).check(matches(isDisplayed()));

    }

    @Test
    public void givenClassesListCardLoads_whenItemRecordAttendanceButtonPressed_shouldLoadAttendanceActivity(){
        Assert.assertTrue(true);
        //TODO: this
        //onView(allOf(withId(R.id.item_clazz)))


        //Assert that a class, the record attendance button opens up the attendance activity for that
        //class with students populated and today's date.
    }

    @Test
    public void givenClassListFragmentStarted_whenClassClicked_shouldLaunchClassDetailActivity(){
        //TODO:
        Assert.assertTrue(true);
        //this

        //Assert that when class cicked goes to its Class Detail activity page.
    }

}
