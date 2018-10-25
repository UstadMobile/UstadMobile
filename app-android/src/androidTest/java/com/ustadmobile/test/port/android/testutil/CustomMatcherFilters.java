package com.ustadmobile.test.port.android.testutil;

import android.graphics.ColorFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;

public class CustomMatcherFilters {

    public static Matcher<Object> withColorFilter(int expectedColor) {
        return withColorFilter(equalTo(expectedColor), expectedColor);
    }

    private static Matcher<Object> withColorFilter(
            final Matcher<Integer> expectedObject, int expectedColor) {

        final ColorFilter[] colorFilter = new ColorFilter[1];

        return new BoundedMatcher<Object, ImageView>( ImageView.class) {

            @Override
            public boolean matchesSafely(final ImageView actualObject) {

                colorFilter[0] = actualObject.getColorFilter();

                //Compare with this one
                ImageView iv = new ImageView(InstrumentationRegistry.getContext());
                iv.setColorFilter(ContextCompat.getColor(InstrumentationRegistry.getContext(),
                        expectedColor));
                ColorFilter ivcf = iv.getColorFilter();

                if(ivcf.equals(colorFilter[0])){
                    return true;
                }else{
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Color Filter did not match " + colorFilter[0]);
            }
        };
    }

}
