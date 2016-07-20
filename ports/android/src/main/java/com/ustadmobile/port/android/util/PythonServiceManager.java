package com.ustadmobile.port.android.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.Context;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import android.os.Handler;
import android.database.Cursor;
import java.util.List;
import java.util.ArrayList;
import android.content.SharedPreferences;
import android.content.Context;

//import org.renpy.android.AudioThread;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.renpy.android.Action;
//import org.renpy.android.AssetExtract;
import org.renpy.android.Configuration;
import org.renpy.android.Hardware;
import org.renpy.android.Project;
import org.renpy.android.PythonActivity;
import org.renpy.android.PythonService;
import org.renpy.android.ResourceManager;
import org.renpy.android.SDLSurfaceView;

import java.io.*;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.util.zip.GZIPInputStream;

import android.content.res.AssetManager;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.view.SplashScreenActivity;

import org.kamranzafar.jtar.*;

/**
 * Created by varuna on 26/11/15.
 * This service starts Python (its part of the Python for Android procedure)
 * This enables the python application to be started on demand
 */
public class PythonServiceManager {

    private static String TAG = "Python";

    // The SDLSurfaceView we contain.
    public static SDLSurfaceView mView = null;
    public static ApplicationInfo mInfo = null;

    // Did we launch our thread?
    private boolean mLaunchedThread = false;

    private ResourceManager resourceManager;

    // The path to the directory contaning our external storage.
    private File externalStorage;

    // The path to the directory containing the game.
    private File mPath = null;

    boolean _isPaused = false;

    private static final String DB_INITIALIZED = "db_initialized";

    public Context context;

    /*Old way of running: Dont think this is ever used..*/
    public void startThis(Context context){
        this.context = context;
        this.externalStorage = new File(Environment.getExternalStorageDirectory(),
                context.getPackageName());
        this.mPath = this.externalStorage;
        run();
        start_service("UstadMobile", "UstadMobile is running",
                "/storage/emulated/0/com.toughra.ustadmobile/lrs-djandro.log");
    }


    public void startThisOnThread(final Context context){

        this.context = context;
        this.externalStorage = new File(Environment.getExternalStorageDirectory(),
                context.getPackageName()); //Gets the external (user accessible directory)
        this.mPath = this.externalStorage;
        final Context finalContext = context;

        Thread extractPythonFiles = new Thread() {
            public void run() {

                //There is no such thing as public. Why is this even here?
                //unpackData("public", externalStorage);
                unpackData("private", finalContext.getFilesDir());

                System.loadLibrary("sdl");
                System.loadLibrary("sdl_image");
                System.loadLibrary("sdl_ttf");
                System.loadLibrary("sdl_mixer");
                System.loadLibrary("python2.7");
                System.loadLibrary("application");
                System.loadLibrary("sdl_main");

                System.load(finalContext.getFilesDir() + "/lib/python2.7/lib-dynload/_io.so");
                System.load(finalContext.getFilesDir() + "/lib/python2.7/lib-dynload/unicodedata.so");

                try {
                    System.loadLibrary("sqlite3");
                    System.load(finalContext.getFilesDir() + "/lib/python2.7/lib-dynload/_sqlite3.so");
                } catch(UnsatisfiedLinkError e) {
                }

                try {
                    System.load(finalContext.getFilesDir() + "/lib/python2.7/lib-dynload/_imaging.so");
                    System.load(finalContext.getFilesDir() + "/lib/python2.7/lib-dynload/_imagingft.so");
                    System.load(finalContext.getFilesDir() + "/lib/python2.7/lib-dynload/_imagingmath.so");
                } catch(UnsatisfiedLinkError e) {
                }

                start_service("UstadMobile", "UstadMobile is running",
                        "/storage/emulated/0/com.toughra.ustadmobile/lrs-djandro.log");

                if(context != null && context.getClass().equals(SplashScreenActivity.class)){
                    System.out.println("Splash screen started this!");
                    UstadMobileSystemImpl.getInstance().startUI(context);
                }


            }
        };
        extractPythonFiles.start();

    }

    public void stop_service() {
        Intent serviceIntent = new Intent(this.context, PythonService.class);
        this.context.stopService(serviceIntent);
    }

    public void start_service(String serviceTitle, String serviceDescription,
                                     String pythonServiceArgument) {
        Intent serviceIntent = new Intent(this.context, PythonService.class);
        String argument = context.getFilesDir().getAbsolutePath();
        //Changes to make it work
        String filesDirectory = this.mPath.getAbsolutePath();
        filesDirectory = context.getFilesDir().getAbsolutePath();
        serviceIntent.putExtra("androidPrivate", argument);
        serviceIntent.putExtra("androidArgument", filesDirectory);
        serviceIntent.putExtra("pythonHome", argument);
        serviceIntent.putExtra("pythonPath", argument + ":" + filesDirectory + "/lib");
        serviceIntent.putExtra("serviceTitle", serviceTitle);
        serviceIntent.putExtra("serviceDescription", serviceDescription);
        serviceIntent.putExtra("pythonServiceArgument", pythonServiceArgument);
        this.context.startService(serviceIntent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("");

    }

    public class AssetExtract {
        private AssetManager mAssetManager = null;

        AssetExtract(Context act) {
            mAssetManager = act.getAssets();
        }

        public boolean extractTar(String asset, String target) {

            byte buf[] = new byte[1024 * 1024];

            InputStream assetStream = null;
            TarInputStream tis = null;

            try {
                assetStream = mAssetManager.open(asset, AssetManager.ACCESS_STREAMING);
                tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(
                        new BufferedInputStream(assetStream, 8192)), 8192));
            } catch (IOException e) {
                Log.e("python", "opening up extract tar", e);
                return false;
            }

            while (true) {
                TarEntry entry = null;

                try {
                    entry = tis.getNextEntry();
                } catch (java.io.IOException e) {
                    Log.e("python", "extracting tar", e);
                    return false;
                }

                if (entry == null) {
                    break;
                }

                Log.i("python", "extracting " + entry.getName());

                if (entry.isDirectory()) {

                    try {
                        new File(target + "/" + entry.getName()).mkdirs();
                    } catch (SecurityException e) {
                    }
                    ;

                    continue;
                }

                OutputStream out = null;
                String path = target + "/" + entry.getName();

                try {
                    out = new BufferedOutputStream(new FileOutputStream(path), 8192);
                } catch (FileNotFoundException e) {
                } catch (SecurityException e) {
                }
                ;

                if (out == null) {
                    Log.e("python", "could not open " + path);
                    return false;
                }

                try {
                    while (true) {
                        int len = tis.read(buf);

                        if (len == -1) {
                            break;
                        }

                        out.write(buf, 0, len);
                    }

                    out.flush();
                    out.close();
                } catch (java.io.IOException e) {
                    Log.e("python", "extracting zip", e);
                    return false;
                }
            }

            try {
                tis.close();
                assetStream.close();
            } catch (IOException e) {
                // pass
            }

            return true;
        }
    }

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmm");//ddMMyyyyHHmm
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    /**
     * This determines if unpacking one the zip files included in
     * the .apk is necessary. If it is, the zip file is unpacked.
     */
    public void unpackData(final String resource, File target) {

        // The version of data in memory and on disk.
        //resourceManager = new ResourceManager(PythonActivity.mActivity.getParent());
        //String data_version = resourceManager.getString(resource + "_version");
        //String data_version = "1448183454.66";
        String data_version = null;
        String disk_version = null;

        String filesDir = target.getAbsolutePath();
        String data_version_fn = filesDir + "/" + resource + ".version";


        // Check the current disk version, if any.
        //GET THE VERSION ON THE APK -DATA VERSION
        try {
            byte buf[] = new byte[64];
            InputStream is = new FileInputStream(data_version_fn);
            int len = is.read(buf);
            data_version = new String(buf, 0, len);
            is.close();
        } catch (Exception e) {
            data_version = "";
            Log.v(TAG, "Could Not get disk version" + e.toString());
        }

        //GET THE VERSION ON DISK (on the phone) -DISK VERSION
        String  disk_version_fn = filesDir + "/"  + "date.extracted";

        try {
            byte buf[] = new byte[64];
            InputStream is = new FileInputStream(disk_version_fn);
            int len = is.read(buf);
            disk_version = new String(buf, 0, len);
            is.close();
        } catch (Exception e) {
            disk_version = "";
            Log.v(TAG, "Could Not get disk version" + e.toString());
        }

        data_version = data_version.replace("\n", "").replace("\r", "");
        disk_version = disk_version.replace("\n", "").replace("\r", "");

        long disk_ver, data_ver;
        try{
            disk_ver = Long.valueOf(disk_version);
            data_ver = Long.valueOf(data_version);
        } catch (Exception e){
            disk_ver = 0;
            data_ver = 0;
        }


        // If the disk data is out of date, extract it and write the
        // version file.
        //OR if disk version was not obtained (we cant make judgements, let it extract)
        //if (! data_version.equals(disk_version) || disk_version == "") {
        if(data_ver > disk_ver || data_ver == 0 || disk_ver == 0){
            Log.v(TAG, "Extracting " + resource + " assets.");

            recursiveDelete(target);
            target.mkdirs();

            //Make a file that marked that you've already extracted this
            disk_version = getCurrentTimeStamp();
            try {
                // Create new date.extracted file if not already..
                new File(target, "date.extracted").delete();
                new File(target, "date.extracted").createNewFile();

                // Write datetime to the file..
                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(disk_version.getBytes());
                os.close();
            } catch (Exception e) {
                Log.w("python", e);
                Log.v(TAG, "Written When extracted to file: date.extracted");
            }

            AssetExtract ae = new AssetExtract(this.context);
            if (!ae.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                System.out.println("Could not extract " + resource + " data.");
            }

            //why are we even creating this ?
            /*
            try {
                // Write .nomedia.
                new File(target, ".nomedia").createNewFile();

                // Write version file.
                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(data_version.getBytes());
                os.close();
            } catch (Exception e) {
                Log.w("python", e);
            }
            */
        }

    }


    /*Seems like we never run this.. */
    public void run() {
        unpackData("private", context.getFilesDir());
        //unpackData("public", externalStorage); //Wont exist

        System.loadLibrary("sdl");
        System.loadLibrary("sdl_image");
        System.loadLibrary("sdl_ttf");
        System.loadLibrary("sdl_mixer");
        System.loadLibrary("python2.7");
        System.loadLibrary("application");
        System.loadLibrary("sdl_main");

        System.load(context.getFilesDir() + "/lib/python2.7/lib-dynload/_io.so");
        System.load(context.getFilesDir() + "/lib/python2.7/lib-dynload/unicodedata.so");

        try {
            System.loadLibrary("sqlite3");
            System.load(context.getFilesDir() + "/lib/python2.7/lib-dynload/_sqlite3.so");
        } catch(UnsatisfiedLinkError e) {
        }

        try {
            System.load(context.getFilesDir() + "/lib/python2.7/lib-dynload/_imaging.so");
            System.load(context.getFilesDir() + "/lib/python2.7/lib-dynload/_imagingft.so");
            System.load(context.getFilesDir() + "/lib/python2.7/lib-dynload/_imagingmath.so");
        } catch(UnsatisfiedLinkError e) {
        }

    }
}
