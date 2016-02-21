package com.ustadmobile.port.android.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int targetWidth= 1400;

    private int targetHeight = 800;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            Camera.Parameters params = mCamera.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                // Autofocus mode is supported
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            //Docs don't say so: but these are all values in landscape
            List<Camera.Size> picSizes = params.getSupportedPictureSizes();

            int bestWidth = -1;
            int bestHeight = -1;
            Camera.Size cSize;
            for(int i = 0; i < picSizes.size(); i++) {
                cSize = picSizes.get(i);
                if(cSize.width >= targetWidth && cSize.height >= targetHeight) {
                    if(bestWidth == -1 ||cSize.width < bestWidth) {
                        bestWidth = cSize.width;
                        bestHeight = cSize.height;
                    }
                }
            }
            params.setPictureSize(bestWidth, bestHeight);
            params.setRotation(90);
            mCamera.setParameters(params);
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);


            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("cwtf", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.


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
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("cwtf", "Error starting camera preview: " + e.getMessage());
        }
    }
}