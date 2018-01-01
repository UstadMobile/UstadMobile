package com.ustadmobile.core.fs.view;

import com.ustadmobile.core.controller.ControllerLifecycleListener;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.AbstractCacheResponse;
import com.ustadmobile.core.impl.HttpCache;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFs;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    /**
     * Hashtable of controller -> Vector of tasks that are associated with that controller
     */
    private Hashtable tasksByController;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private class ImageLoaderTask implements UmHttpResponseCallback, Runnable{

        String url;

        ImageLoadTarget target;

        UstadBaseController controller;

        private boolean cacheOnlyCheckComplete;

        private boolean responseIsFresh = false;

        private HttpCache cache;

        private UmHttpCall currentCall;

        private boolean cancelled = false;

        private volatile AbstractCacheResponse responseBuffer;

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

                currentCall = cache.get(
                        new UmHttpRequest(controller.getContext(), url).setOnlyIfCached(true),
                        this);
            }
        }

        private synchronized void handleCacheResponded() {
            cacheOnlyCheckComplete = true;
            if(cancelled)
                return;

            if(!responseIsFresh)
                currentCall = cache.get(new UmHttpRequest(controller.getContext(), url), this);
        }

        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            synchronized (this) {
                this.responseBuffer = (AbstractCacheResponse)response;
                responseIsFresh = responseBuffer.isFresh();
            }

            executorService.execute(this);
        }

        public void run() {
            if(!responseBuffer.isNetworkResponseNotModified()) {
                try {
                    target.setImageFromBytes(responseBuffer.getResponseBody());
                }catch(IOException e) {
                    e.printStackTrace();
                }catch(IllegalStateException ie) {
                    ie.printStackTrace();
                }
            }

            responseBuffer = null;

            if(cacheOnlyCheckComplete) {
                //task is complete
                removeTaskForController(controller, this);
            }else {
                handleCacheResponded();
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {
            if(!cacheOnlyCheckComplete) {
                UstadMobileSystemImpl.l(UMLog.INFO, 0, this + " not cached");
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
     * Represents a consumer that expects to display the image loaded.
     */
    public interface ImageLoadTarget {


        /**
         * Called when image data is available. This may be called up to two times:
         *  Firstly if a cached version of the image is available, called before the entry has been validated
         *  Secondly if a network request yields an updated version of the image, or if no cached version
         *  was available
         *
         *  This method will be called on an executorservice thread seperate to the UI. It is thus
         *  suitable for performing decoding directly in the implementation of this method.
         *
         * @param bytes Image data as a byte buffer as it was returned from the network or cache
         */
        void setImageFromBytes(byte[] bytes);

    }

    public ImageLoader() {
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
