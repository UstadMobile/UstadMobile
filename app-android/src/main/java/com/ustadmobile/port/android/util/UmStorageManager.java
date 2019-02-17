package com.ustadmobile.port.android.util;

import android.content.Context;
import android.os.StatFs;
import android.os.storage.StorageManager;

import com.ustadmobile.port.sharedse.util.UmStorageManager;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class which used reflection to get all storage on the device
 * i.e External device storage and SD card.
 *
 * @author kileha3
 *
 * @link https://gist.github.com/PauloLuan/4bcecc086095bce28e22
 */
public class UmStorageManagerAndroid extends UmStorageManager {

    private Context mContext;

    public UmStorageManagerAndroid(Object context) {
        super(context);
        this.mContext = (Context) context;
    }

    /**
     * Retrieve storage using android file system
     * @param removable Flag to check if the storage is removable or not.
     * @return Storage file
     */
    private File getStorage(boolean removable) {
        StorageManager mStorageManager =
                (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                if (removable == (Boolean) isRemovable.invoke(storageVolumeElement)) {
                    return new File(path);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UmStorage getStorage(int type) {
        File externalStorage = getStorage(false);
        File sdCardStorage = getStorage(true);

        switch (type){
            case STORAGE_TYPE_EXTERNAL:
                return  new UmStorage(externalStorage,
                        externalStorage.length(),getFreeSize(externalStorage));
            case STORAGE_TYPE_SDCARD:
                return  new UmStorage(sdCardStorage,
                        sdCardStorage.length(),getFreeSize(sdCardStorage));
        }
        return null;
    }

    private long getFreeSize(File storage){
        StatFs stat = new StatFs(storage.getPath());
        return stat.getBlockSizeLong() * stat.getBlockCountLong();
    }

}
