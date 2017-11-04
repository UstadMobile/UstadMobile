package com.ustadmobile.core.view;

import com.ustadmobile.core.controller.ControllerLifecycleListener;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.HTTPCacheDir;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Ustad Mobile Image Loader. The Image Loader is cross platform and should work on any platform
 * where file I/O is supported. It uses the existing HTTPCacheDir class.
 *
 * The ImageLoader will first display any cached copy of an image that exists (to improve responsiveness
 * for the user). It will then use the main http cache method to validate or download the image as
 * required. This is done using two separate threads each managed by a Timer.
 *
 * To receive an Image the destination must implement ImageViewLoadTarget. Typical usage would be
 * along the lines of:
 *
 * ImageLoader.getInstance().loadImage("http://path/to/image.jpg", new ImageLoader.ImageLoadTarget() {
 *     public void setImageFromFile(String filePath) {
 *         //set the image itself as per platform requirements
 *     }
 * }, controller);
 *
 * Where the controller is the controller in which this image is displayed. If the controller is
 * destroyed the loading of the image will be cancelled.
 *
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
            HTTPCacheDir cacheDir = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(controller.getContext());
            String fileUri = cacheDir.getCacheFileURIByURL(url);
            try {
                if(fileUri != null && UstadMobileSystemImpl.getInstance().fileExists(fileUri)){
                    target.setImageFromFile(fileUri);
                }
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 657, url, e);
            }

            Vector controllerTasks = removeTaskForController(controller, this);
            if(controllerTasks == null)
                return;//controller has been destroyed

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
                //TODO: add image loader tests for handling file paths
                if(url.startsWith("file:/") || url.startsWith("/")) {
                    url = UMFileUtil.stripPrefixIfPresent("file:///", url);
                    target.setImageFromFile(url);
                }else {
                    HTTPCacheDir cacheDir = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(
                            controller.getContext());
                    String filePath = cacheDir.get(url);
                    Vector activeTasks = removeTaskForController(controller, this);
                    if(filePath != null && activeTasks != null) {
                        target.setImageFromFile(filePath);
                    }
                }
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 658, url, e);
            }
        }
    }

    private static ImageLoader singleton = new ImageLoader();

    public static ImageLoader getInstance() {
        return singleton;
    }

    /**
     *
     */
    public interface ImageLoadTarget {

        void setImageFromFile(String filePath);

    }

    public ImageLoader() {
        cacheLoadTimer = new Timer();
        networkLoadTimer = new Timer();
        tasksByController = new Hashtable();
    }

    /**
     * Loads the image as per the specified src. Wehn the image is available the target will be
     * given a fill path where the image is saved to
     *
     * @param src The url of where to download the image from.
     * @param target An ImageLoadTarget which can display an image. E.g. use a wrapper around an ImageView etc.
     * @param controller The controller in which this image is being displayed. If the controller
     *                   is destroyed, the load will be canceled.
     */
    public void loadImage(final String src, final ImageLoadTarget target, final UstadBaseController controller) {
        Vector controllerTasks;
        synchronized (tasksByController) {
            controllerTasks = getTasksByController(controller);
            if(controllerTasks == null) {
                controllerTasks = new Vector();
                tasksByController.put(controller, controllerTasks);
            }
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

    /**
     *
     * @param controller
     * @param task
     * @return
     */
    private Vector removeTaskForController(UstadBaseController controller, ImageLoaderTask task) {
        Vector controllerTaskVector = getTasksByController(controller);
        if(controllerTaskVector == null)
            return null;

        synchronized (controllerTaskVector) {
            if(controllerTaskVector.indexOf(task) != -1) {
                controllerTaskVector.removeElement(task);
                return controllerTaskVector;
            }
        }

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
