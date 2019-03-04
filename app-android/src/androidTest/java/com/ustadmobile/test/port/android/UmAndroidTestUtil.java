package com.ustadmobile.test.port.android;

import android.os.Environment;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;

import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.test.port.android.view.VideoPlayerTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Pattern;

public class UmAndroidTestUtil {

    /**
     * Set Airplane mode on to test reaction to the system going offline
     *
     * @param enabled
     */
    public static void setAirplaneModeEnabled(boolean enabled) {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int deviceHeight = uiDevice.getDisplayHeight();
        uiDevice.swipe(100, 0, 100, deviceHeight / 2, 10);
        SystemClock.sleep(200);
        uiDevice.swipe(100, 0, 100, deviceHeight / 2, 10);

        //see what the state is now
        UiObject2 airplaneModeObject = uiDevice.findObject(By.descContains("plane"));
        if (airplaneModeObject == null) {
            airplaneModeObject = uiDevice.findObject(By.descContains("Flight"));
        }

        if (airplaneModeObject == null)
            throw new IllegalStateException("Could not find flight mode button");

        String contentDesc = airplaneModeObject.getContentDescription();

        if (isAirPlaneModeOn(contentDesc) != enabled || !isAirPlaneModeSupported(contentDesc))
            airplaneModeObject.click();

        SystemClock.sleep(100);
        uiDevice.pressBack();
        SystemClock.sleep(100);
        uiDevice.pressBack();
    }

    private static boolean isAirPlaneModeOn(String contentDesc) {
        if (isAirPlaneModeSupported(contentDesc)) {
            for (String desc : contentDesc.split(",")) {
                if (desc.toLowerCase().startsWith("on")) return true;
            }
        }
        return false;
    }

    private static boolean isAirPlaneModeSupported(String contentDesc) {
        String[] descs = contentDesc.split(",");
        boolean supported = descs.length > 1;
        for (String desc : descs) {
            if (desc.toLowerCase().startsWith("on") || desc.toLowerCase().startsWith("off")) {
                supported = true;
            }
        }
        return supported;
    }

    public static File readFromTestResources(String pathToFile, String nameOfFile) throws IOException {
        InputStream inputStream = VideoPlayerTest.class.getResourceAsStream(pathToFile);
        File path = Environment.getExternalStorageDirectory();
        File targetFile = new File(path, nameOfFile);
        OutputStream outStream = new FileOutputStream(targetFile);
        UMIOUtils.readFully(inputStream, outStream);
        if (inputStream != null) {
            inputStream.close();
        }
        outStream.flush();
        outStream.close();

        return targetFile;
    }

    public static void readAllFilesInDirectory(File directory, HashMap<File, String> filemap) {
        Path sourceDirPath = Paths.get(directory.toURI());
        try {
            Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        String relativePath = sourceDirPath.relativize(path).toString()
                                .replaceAll(Pattern.quote("\\"), "/");
                        filemap.put(path.toFile(), relativePath);

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
