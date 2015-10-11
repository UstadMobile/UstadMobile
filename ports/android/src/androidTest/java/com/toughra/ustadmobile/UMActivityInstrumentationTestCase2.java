package com.toughra.ustadmobile;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by mike on 9/29/15.
 */
public class UMActivityInstrumentationTestCase2<T extends Activity> extends ActivityInstrumentationTestCase2 {

    private T mActivity;

    private Class mActivityClass;

    public UMActivityInstrumentationTestCase2(Class activityClass) {
        super(activityClass);
        mActivityClass = activityClass;
    }

    /**
     * Because the default implementation is a horrible blocking piece of junk
     * http://stackoverflow.com/questions/20860832/why-does-getactivity-block-during-junit-test-when-custom-imageview-calls-start
     *
     * @return
     */
    @Override
    public T getActivity() {
        if (mActivity == null) {
            Intent intent = new Intent(getInstrumentation().getTargetContext(), UstadMobileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // register activity that need to be monitored.
            Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(mActivityClass.getName(), null, false);
            getInstrumentation().getTargetContext().startActivity(intent);
            mActivity = (T)(UstadMobileActivity) getInstrumentation().waitForMonitor(monitor);
            setActivity(mActivity);
        }
        return mActivity;
    }




}
