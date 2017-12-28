package com.ustadmobile.core.fs.view;

import com.ustadmobile.core.controller.ControllerLifecycleListener;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.AbstractCacheResponse;
import com.ustadmobile.core.impl.HttpCache;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFs;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
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

    private class ImageLoaderTask implements UmHttpResponseCallback{

        String url;

        ImageLoadTarget target;

        UstadBaseController controller;

        private boolean cacheResponded;

        private HttpCache cache;

        private UmHttpCall currentCall;

        private boolean cancelled = false;

        private ImageLoaderTask(String url, ImageLoadTarget target, UstadBaseController controller) {
            this.url = url;
            this.target = target;
            this.controller = controller;
            cache = ((UstadMobileSystemImplFs)UstadMobileSystemImpl.getInstance()).getHttpCache(
                    controller.getContext());
        }

        private void load() {
            synchronized (this) {
                if(cancelled)
                    return;

                currentCall = cache.get(new UmHttpRequest(url).setOnlyIfCached(true), this);
            }
        }

        private void handleCacheResponded() {
            cacheResponded = true;
            synchronized (this) {
                if(cancelled)
                    return;

                currentCall = cache.get(new UmHttpRequest(url), this);
            }
        }

        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            if(!cacheResponded) {
                handleCacheResponded();
            }else {
                //task is complete
                removeTaskForController(controller, this);
            }

            //TODO: check if the image actually changed since the cached reply
            target.setImageFromFile(((AbstractCacheResponse)response).getFilePath());
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {
            if(!cacheResponded) {
                handleCacheResponded();
            }
        }

        public synchronized void cancel() {
            cancelled = true;
            if(currentCall != null)
                currentCall.cancel();
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

        ImageLoaderTask cachedImageTask = new ImageLoaderTask(src, target, controller);
        controllerTasks.addElement(cachedImageTask);
        cachedImageTask.load();
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
