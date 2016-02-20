package com.ustadmobile.test.port.android;

import android.app.ActivityManager;
import android.content.Context;
import org.renpy.android.PythonService;

/**
 * Created by varuna on 19/02/16.
 */
public class UMAndroidTestUtil {

    class PythonServiceNotRunning extends Exception {
        public PythonServiceNotRunning(String msg){
            super(msg);
        }
    }

    private static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean waitForPythonService(Context context) throws Exception{
        //This is to wait for Python service and to see
        System.out.println("Waiting for python service is started or not..");

        int TIMEOUT_COUNT=40;
        int COUNT_NOW=0;
        do{
            try {
                Thread.sleep(1000); //sleep for one second
            }catch(Exception e){
                System.out.println(e.toString());
            }
            COUNT_NOW = COUNT_NOW + 1;
            if (isMyServiceRunning(PythonService.class, context)) {
                System.out.println("TestUtils.waitForPythonService: Already running.");
                return true;

            }else{
                if (COUNT_NOW<TIMEOUT_COUNT){
                    return false;
                }

            }

        } while ( COUNT_NOW < TIMEOUT_COUNT );
        return false;

    }

}
