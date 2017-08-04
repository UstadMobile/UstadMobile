package com.ustadmobile.core.view;

import com.ustadmobile.core.controller.ControllerLifecycleListener;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.HTTPCacheDir;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by mike on 8/3/17.
 */

public class ImageLoader implements ControllerLifecycleListener {

    private Timer cacheLoadTimer;

    private Timer networkLoadTimer;

    /**
     * Hashtable of controller -> Vector of tasks that are associated with that controller
     */
    private Hashtable tasksByController;

    private abstract class ImageLoaderTask extends TimerTask {

        String url;

        ImageLoadTarget target;

        Object context;

        UstadBaseController controller;

        private ImageLoaderTask(String url, ImageLoadTarget target, UstadBaseController controller) {
            this.url = url;
            this.target = target;
            this.controller = controller;
        }

    }

    private class LoadCachedImageTask extends ImageLoaderTask {

        private LoadCachedImageTask(String url, ImageLoadTarget target, UstadBaseController controller){
            super(url, target, controller);
        }

        public void run() {
            HTTPCacheDir cacheDir = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(context);
            String fileUri = cacheDir.getCacheFileURIByURL(url);
            try {
                if(fileUri != null && UstadMobileSystemImpl.getInstance().fileExists(fileUri)){
                    target.setImageFromFile(fileUri);
                }
            }catch(IOException e) {

            }

            Vector controllerTasks = getTasksByController(controller);
            controllerTasks.removeElement(this);
            LoadImageFromNetworkTask networkTask = new LoadImageFromNetworkTask(url, target, controller);
            controllerTasks.addElement(networkTask);
            networkLoadTimer.schedule(networkTask, 0);
        }
    }


    private class LoadImageFromNetworkTask extends ImageLoaderTask {

        public LoadImageFromNetworkTask(String url, ImageLoadTarget target, UstadBaseController controller) {
            super(url, target, controller);
        }

        public void run() {
            try {
                HTTPCacheDir cacheDir = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(context);
                String filePath = cacheDir.get(url);
                if(filePath != null) {
                    target.setImageFromFile(filePath);
                }
                getTasksByController(controller).removeElement(this);
            }catch(IOException e) {

            }
        }
    }

    private static ImageLoader singleton = new ImageLoader();

    public static ImageLoader getInstance() {
        return singleton;
    }

    public interface ImageLoadTarget {

        void setImageFromFile(String filePath);

    }

    public ImageLoader() {
        cacheLoadTimer = new Timer();
        networkLoadTimer = new Timer();
        tasksByController = new Hashtable();
    }

    public void loadImage(String src, ImageLoadTarget target, UstadBaseController controller) {
        Vector controllerTasks = getTasksByController(controller);
        if(controllerTasks == null) {
            controllerTasks = new Vector();
            tasksByController.put(controller, controllerTasks);
        }

        controller.addLifecycleListener(this);

        LoadCachedImageTask cachedImageTask = new LoadCachedImageTask(src, target, controller);
        controllerTasks.addElement(cachedImageTask);
        cacheLoadTimer.schedule(cachedImageTask, 0);
    }

    private Vector getTasksByController(UstadBaseController controller) {
        if(tasksByController.containsKey(controller))
            return (Vector)tasksByController.get(controller);
        else
            return null;
    }

    public void onStarted(UstadBaseController controller) {

    }

    public void onStopped(UstadBaseController controller) {

    }

    public void onDestroyed(UstadBaseController controller) {
        Vector controllerTasks = getTasksByController(controller);
        if(controllerTasks != null) {
            synchronized (controllerTasks) {
                ImageLoaderTask task;
                for (int i = 0; i < controllerTasks.size(); i++) {
                    task = (ImageLoaderTask)controllerTasks.elementAt(i);
                    task.cancel();
                }
            }

            tasksByController.remove(controller);
        }

        controller.removeLifecycleListener(this);
    }
}
