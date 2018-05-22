package com.ustadmobile.port.android.view;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * A basic Camera preview class
 *
 * Based on https://developer.android.com/guide/topics/media/camera.html#custom-camera
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.PreviewCallback mPreviewCallback;
    private PreviewStartedCallback mPreviewStartCallback;


    private boolean mPreviewActive = false;



    public CameraPreview(Context context, PreviewStartedCallback previewStartCallback) {
        super(context);
        mPreviewStartCallback = previewStartCallback;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public Camera.PreviewCallback getPreviewCallback() {
        return mPreviewCallback;
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        previewIfReady();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mHolder = holder;
        previewIfReady();
    }

    public void previewIfReady() {
        boolean focusMoveCallbackStarted = false;
        if(!mPreviewActive && mCamera != null && mHolder != null) {
            //good to go now
            if(mPreviewCallback != null) {
                mCamera.setPreviewCallback(mPreviewCallback);
            }

            try {
                //Camera.Parameters params = mCamera.getParameters();
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                mPreviewActive = true;

                if(mPreviewStartCallback != null) {
                    mPreviewStartCallback.onPreviewStarted(this, mCamera);
                }
            } catch (IOException e) {
                Log.d("cwtf", "Error setting camera preview: " + e.getMessage());
            }
        }
    }

    public void stopAndRelease() {
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
        mPreviewActive = false;
    }



    public void surfaceDestroyed(SurfaceHolder holder) {
        //stopping camera must be handled in onPause of Fragment or Activity
        mHolder = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        mHolder = holder;

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            if(mPreviewCallback != null) {
                mCamera.setPreviewCallback(mPreviewCallback);
            }
            mCamera.getParameters().setPreviewFormat(ImageFormat.NV21);

            mCamera.startPreview();

        } catch (Exception e){
            Log.d("cwtf", "Error starting camera preview: " + e.getMessage());
        }
    }

    public static interface PreviewStartedCallback {

        public void onPreviewStarted(CameraPreview preview, Camera camera);

    }

}