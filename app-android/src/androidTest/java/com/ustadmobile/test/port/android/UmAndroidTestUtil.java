package com.ustadmobile.test.port.android;

import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;

public class UmAndroidTestUtil {

    /**
     * Set Airplane mode on to test reaction to the system going offline
     *
     * @param enabled
     */
    public static void setAirplaneModeEnabled(boolean enabled) {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int deviceHeight = uiDevice.getDisplayHeight();
        uiDevice.swipe(100, 0, 100, deviceHeight/2, 10);
        SystemClock.sleep(200);
        uiDevice.swipe(100, 0, 100, deviceHeight/2, 10);

        //see what the state is now
        UiObject2 airplaneModeObject = uiDevice.findObject(By.descContains("plane"));
        boolean airplaneModeState = airplaneModeObject.getContentDescription().contains("on");
        if(airplaneModeState != enabled)
            airplaneModeObject.click();

        SystemClock.sleep(100);
        uiDevice.pressBack();
        SystemClock.sleep(100);
        uiDevice.pressBack();
    }


}
