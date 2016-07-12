package com.ustadmobile.port.android.view;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.model.AttendanceSheetImage;
import com.ustadmobile.core.omr.OMRImageSource;
import com.ustadmobile.core.omr.OMRRecognizer;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.android.impl.UMLogAndroid;
import com.ustadmobile.port.android.impl.qr.NV21OMRImageSource;
import com.ustadmobile.port.android.impl.qr.RotatedNV21OMRImageSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.sourceforge.qrcode.geom.Point;
import jp.sourceforge.qrcode.pattern.FinderPattern;
import jp.sourceforge.qrcode.reader.QRCodeImageReader;

import static com.ustadmobile.core.model.AttendanceSheetImage.DEFAULT_PAGE_X_DISTANCE;
import static com.ustadmobile.core.model.AttendanceSheetImage.DEFAULT_PAGE_Y_DISTANCE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceCameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceCameraFragment extends Fragment implements View.OnClickListener, Camera.PreviewCallback, View.OnTouchListener, CameraPreview.PreviewStartedCallback, AttendanceSheetImage.DebugSaveRequestListener, AttendanceSheetImage.SheetRecognizedListener {

    private CameraPreview mPreview;

    private Camera mCamera;

    private String mCurrentImgPath;

    private byte[] mPreviewFrameBuffer;

    private Object lockObj;

    private RectangleView mRectangleView;

    private AttendanceSheetImage mSheet;

    private RotatedNV21OMRImageSource mOMRImageSource;

    private Camera.Size mCamPreviewSize;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AttendanceCameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AttendanceCameraFragment newInstance() {
        AttendanceCameraFragment fragment = new AttendanceCameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AttendanceCameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        Camera.Parameters params = c.getParameters();
        mCamPreviewSize = params.getPreviewSize();
        mPreviewFrameBuffer = new byte[mCamPreviewSize.width * mCamPreviewSize.height];
        mOMRImageSource = new RotatedNV21OMRImageSource(mCamPreviewSize.width, mCamPreviewSize.height);

        //set exposure compensation because it's white paper
        float targetEV = 5f/3f;
        int step = Math.round(targetEV/params.getExposureCompensationStep());
        params.setExposureCompensation(step);

        mSheet = new AttendanceSheetImage();
        mSheet.setImageSource(mOMRImageSource);
        //mSheet.setOnDebugSaveRequestListener(this);
        mSheet.setOnSheetRecognizedListener(this);

        /*
          Because the AttendanceSheet itself is using RotatedNV21OMRImage it's thinking in terms
          of a sheet that's already been rotated.  Areas specified for the camera must be as the
         */
        float[] rotatedDistances = mSheet.getPageDistances();
        rotatedDistances = new float[]{
                rotatedDistances[OMRRecognizer.RIGHT], rotatedDistances[OMRRecognizer.TOP],
                rotatedDistances[OMRRecognizer.LEFT], rotatedDistances[OMRRecognizer.BOTTOM]
        };

        float finderPatternSize = (mSheet.getFinderPatternSize() * mCamPreviewSize.height)/(float)mCamPreviewSize.width;
        AttendanceSheetImage rotatedSheet = new AttendanceSheetImage(mSheet.getPageAreaMargin(),
                AttendanceSheetImage.DEFAULT_PAGE_HEIGHT, AttendanceSheetImage.DEFAULT_PAGE_WIDTH,
                rotatedDistances, finderPatternSize);
        rotatedSheet.calcExpectedAreas(mCamPreviewSize.width, mCamPreviewSize.height);

        ArrayList<Camera.Area> meterList = new ArrayList<>();
        meterList.add(new Camera.Area(omrIntsToRect(rotatedSheet.getExpectedPageArea()), 1000));
        params.setMeteringAreas(meterList);

        ArrayList<Camera.Area> focusList = new ArrayList<>();
        int[][] fpAreas = rotatedSheet.getFinderPatternSearchAreas();
        int maxFocusAreas = c.getParameters().getMaxNumFocusAreas();
        for(int i = 0; i < fpAreas.length && i < maxFocusAreas; i++) {
            focusList.add(new Camera.Area(omrIntsToRect(fpAreas[i]), 1000));
        }
        params.setMeteringAreas(focusList);
        c.setParameters(params);

        return c; // returns null if camera is unavailable
    }


    public static int[] rotateOMRRect(int[] rect) {
        int[] result = new int[] {
            rect[OMRRecognizer.Y]
        };


        return result;
    }

    /**
     * Converts an integer array representing a rectangle as used by the OMR methods into an
     * Android Rect object
     *
     * @param rect Array of 4 integer: x, y, width, height as returned by OMR methods
     *
     * @return Android rect object
     */
    public static Rect omrIntsToRect(int[] rect) {
        return new Rect(rect[OMRRecognizer.X], rect[OMRRecognizer.Y],
                rect[OMRRecognizer.X] + rect[OMRRecognizer.WIDTH],
                rect[OMRRecognizer.Y] + rect[OMRRecognizer.HEIGHT]);
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "attendance_" + timestamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentImgPath = image.getAbsolutePath();
        return image;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        lockObj = new Object();
        View view = inflater.inflate(R.layout.fragment_attendance_camera, container, false);
        mCamera = getCameraInstance();
        FrameLayout preview = (FrameLayout)view.findViewById(R.id.fragment_attendance_camera_preview);
        View rectView = view.findViewById(R.id.fragment_attendance_rectangleview);
        rectView.setOnTouchListener(this);

        mPreview = new CameraPreview(getContext(), mCamera, this, this);
        //mPreview.setOnTouchListener(this);

        mRectangleView = (RectangleView)view.findViewById(R.id.fragment_attendance_rectangleview);

        preview.addView(mPreview, 0);
        //Button captureButton = (Button)view.findViewById(R.id.fragment_attendance_camera_capture);
        //captureButton.setOnClickListener(this);



        return view;
    }


    protected void handleImageCaptured(String fileURI) {
        ((AttendanceActivity)getActivity()).processImage(fileURI);
    }

    @Override
    public void sheetRecognized(AttendanceSheetImage sheet) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getContext(), "Sheet recognized", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        mCamera.takePicture(null, null, mPicture);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = null;
            Exception e = null;
            try {
                pictureFile = createImageFile();
            }catch(IOException ioe) {
                e = ioe;
            }

            if (pictureFile == null){
                Log.d("cwtf", "Error creating media file, check storage permissions: " +
                        e.getMessage());
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                AttendanceCameraFragment.this.handleImageCaptured(pictureFile.getAbsolutePath());
            } catch (FileNotFoundException fe) {
                Log.d("cwtf", "File not found: " + fe.getMessage());
            } catch (IOException ioe) {
                Log.d("cwtf", "Error accessing file: " + ioe.getMessage());
            }
        }
    };

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        mSheet.updateImageSource(bytes);
    }

    /**
     * When the preview surface is ready start checking for output
     * @param preview
     * @param camera
     */
    @Override
    public void onPreviewStarted(CameraPreview preview, Camera camera) {
        mSheet.startChecking();
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                saveDebugImage(mSheet, mSheet.getImageSource());
                break;
        }

        return false;
    }

    @Override
    public void saveDebugImage(AttendanceSheetImage sheet, OMRImageSource omrImgSrc) {

        RotatedNV21OMRImageSource imgSrc = (RotatedNV21OMRImageSource)omrImgSrc;
        int[] imgBuf = new int[mCamPreviewSize.height * mCamPreviewSize.width];
        imgSrc.decodeGrayscale(imgBuf, 0, 0, mCamPreviewSize.height, mCamPreviewSize.width);
        Bitmap imgBitmap = Bitmap.createBitmap(imgBuf, mCamPreviewSize.height, mCamPreviewSize.width,
                Bitmap.Config.ARGB_8888);
        AndroidDebugCanvas dc = new AndroidDebugCanvas(imgBitmap);
        mSheet.drawAreas(dc);

        File debugDestFile = new File(Environment.getExternalStorageDirectory(),
                "ustadmobileContent/attendance/gray-debug-" + ".jpg");
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(debugDestFile);
            dc.getMutableBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fout);
            fout.flush();
        }catch(Exception e) {
            Log.e(UMLogAndroid.LOGTAG, debugDestFile.getAbsolutePath(), e);
        }finally {
            if(fout != null) {
                try { fout.close(); }
                catch(IOException e) {Log.e(UMLogAndroid.LOGTAG, debugDestFile.getAbsolutePath(), e);}
            }
        }

        //save an image of what the whole bitmap looks like
        int[][] gsBuf = new int[mCamPreviewSize.height][mCamPreviewSize.width];
        imgSrc.getGrayscaleImage(gsBuf, 0, 0, mCamPreviewSize.height, mCamPreviewSize.width);
        debugDestFile = new File(Environment.getExternalStorageDirectory(),
                "ustadmobileContent/attendance/gray-debug-bw" + ".jpg");
        boolean[][] imgBoolean = QRCodeImageReader.grayScaleToBitmap(gsBuf);
        Point[] fPatterns = FinderPattern.findCenters(imgBoolean);
        if(fPatterns != null && fPatterns.length > 0) {
            Log.i(UMLogAndroid.LOGTAG, "Found patterns on image");
        }
        try {
            fout = new FileOutputStream(debugDestFile);
            AndroidDebugCanvas.booleanArrayToBitmap(imgBoolean).compress(
                    Bitmap.CompressFormat.JPEG, 100, fout);
            fout.flush();
        }catch(IOException e) {
            UMIOUtils.closeOutputStream(fout);
        }


        //now save each of the areas that it should be looking at
        int[][] fpSearchAreas = mSheet.getFinderPatternSearchAreas();
        int fpSizePx = fpSearchAreas[0][OMRRecognizer.WIDTH];
        int[][] fpImg = new int[fpSizePx][fpSizePx];
        Bitmap fpAreaBM;
        Bitmap fpAreaBW;
        File fpFile;
        boolean[][] fpBits = new boolean[fpSizePx][fpSizePx];
        for(int i = 0; i < fpSearchAreas.length; i++) {
            imgSrc.getGrayscaleImage(fpImg, fpSearchAreas[i][OMRRecognizer.X],
                    fpSearchAreas[i][OMRRecognizer.Y],
                    fpSearchAreas[i][OMRRecognizer.WIDTH],
                    fpSearchAreas[i][OMRRecognizer.HEIGHT]);
            fpAreaBM = AndroidDebugCanvas.intArrayToImage(fpImg);
            fpFile = new File(Environment.getExternalStorageDirectory(),
                    "ustadmobileContent/attendance/gray-debug-fparea" + i + ".jpg");
            try {
                fout = new FileOutputStream(fpFile);
                fpAreaBM.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.flush();
            }catch(IOException e) {
                Log.e(UMLogAndroid.LOGTAG, "Exception saving debug image", e);
            }finally {
                UMIOUtils.closeOutputStream(fout);
            }

            //fpBits = QRCodeImageReader.grayScaleToBitmap(fpImg);
            grayScaleToBitmap(fpImg, fpBits, 128);
            fpAreaBW = AndroidDebugCanvas.booleanArrayToBitmap(fpBits);

            Point[] patternCenters = FinderPattern.findCenters(fpBits);
            if(patternCenters != null && patternCenters.length > 0) {
                Log.i(UMLogAndroid.LOGTAG, "HOORAH!");
            }


            fpFile = new File(Environment.getExternalStorageDirectory(),
                    "ustadmobileContent/attendance/gray-debug-fparea" + i + "-bw.jpg");
            try {
                fout = new FileOutputStream(fpFile);
                fpAreaBW.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.flush();
            }catch(IOException e) {
                Log.e(UMLogAndroid.LOGTAG, "Exception saving debug image", e);
            }finally {
                UMIOUtils.closeOutputStream(fout);
            }
        }

    }

    public static void grayScaleToBitmap(int[][] gs, boolean[][] out, int threshold) {
        int x, y, l;
        for(x = 0; x < gs.length; x++) {
            for(y = 0; y < gs[0].length; y++) {
                out[x][y] = (gs[x][y] & 0xFF) < threshold;
            }
        }
    }


    public void processYuvImage() {
        //see
        // http://stackoverflow.com/questions/5272388/extract-black-and-white-image-from-android-cameras-nv21-format/12702836#12702836
        long procTime = -1;
        long patternTime = -1;
        long decodeTime = -1;
        int[] pixels;
        byte[] imgBuffer;
        synchronized (lockObj) {
            imgBuffer = mPreviewFrameBuffer;
        }
        long time = System.currentTimeMillis();
        //mSheet.isAligned();
        procTime = System.currentTimeMillis();
        System.out.println("time = " + procTime  + "ms");

        /*
        try {
            //lock.lock();
            Camera.Parameters params = mCamera.gewwtParameters();
            Camera.Size size = params.getPreviewSize();
            AttendanceSheetImage sheetImage = new AttendanceSheetImage();
            NV21OMRImageSource imgSource = new RotatedNV21OMRImageSource(size.width, size.height);
            imgSource.setNV21Buffer(mPreviewFrameBuffer);
            sheetImage.setImageSource(imgSource);

            int[] imgBuf = new int[size.width * size.height];
            imgSource.decodeGrayscale(imgBuf, 0, 0, size.height, size.width);
            Bitmap imgBitmap = Bitmap.createBitmap(imgBuf, size.height, size.width,
                    Bitmap.Config.ARGB_8888);
            AndroidDebugCanvas dc = new AndroidDebugCanvas(imgBitmap);
            sheetImage.drawAreas(dc);
            File cropDestFile = new File(Environment.getExternalStorageDirectory(),
                    "ustadmobileContent/attendance/gray-debug-" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fout = new FileOutputStream(cropDestFile);
            dc.getMutableBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fout);
            fout.flush();
            fout.close();



            /*
            time = System.currentTimeMillis();
            QRCodeImage qrImg = UstadMobileSystemImpl.getInstance().getQRCodeImage(bm);
            boolean[][] bitmap = OMRRecognizer.convertImgToBitmap(qrImg);
            Point[] pt = FinderPattern.findCenters(bitmap);
            patternTime = System.currentTimeMillis() - time;

        }catch(Exception e) {
            Log.e(UMLogAndroid.LOGTAG, "shisse", e);
            e.printStackTrace();
        }finally {
            Log.i(UMLogAndroid.LOGTAG, "Process time = " + procTime +
                    " pattern find time = " + patternTime +
                    " deocde time = " + decodeTime);
            //lock.unlock();
        }
        */

    }
}
