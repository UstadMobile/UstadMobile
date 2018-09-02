package com.ustadmobile.test.port.android.testutil;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

/**
 * Helpers for dealing with recycler view in UI testing.
 *
 */
public class RecyclerViewChildAction {

    /**
     * Perform a click on the given descendant view rather than the matched view itself.
     *
     * @param id The descendant view id to be clicked
     * @return ViewAction
     */
    public static ViewAction clickOnDescendantViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Use findViewById to click on the descendant with the given id";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.findViewById(id).performClick();
            }
        };
    }

}
