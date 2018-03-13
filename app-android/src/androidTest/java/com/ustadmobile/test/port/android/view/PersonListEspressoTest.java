package com.ustadmobile.test.port.android.view;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.view.BasePointActivity;

import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by mike on 3/8/18.
 */

public class PersonListEspressoTest {

    @Rule
    public ActivityTestRule<BasePointActivity> mActivityRule =
            new ActivityTestRule<>(BasePointActivity.class);

//    DISABLED until this is built
//    @Test
    public void testAddUser() {
        String newName = UUID.randomUUID().toString();
        onView(withId(R.id.activity_basepoint_drawlayout)).perform(DrawerActions.open());
        onView(allOf(withText("People"), isDescendantOfA(withId(R.id.activity_basepoint_navigationview))))
                .perform(click());
        onView(withId(R.id.activity_basepoint_fab)).perform(click());
        onView(withId(R.id.fragment_person_edit_first_name)).perform(typeText(newName));
        onView(withContentDescription(R.string.ok)).perform(click());

        //should now be on the person overview
        onView(withId(R.id.fragment_person_first_name)).check(matches(withText(newName)));


    }

}
