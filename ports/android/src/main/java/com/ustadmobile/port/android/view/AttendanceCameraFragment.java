package com.ustadmobile.port.android.view;


import android.graphics.Bitmap;
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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.model.AttendanceSheetImage;
import com.ustadmobile.port.android.impl.UMLogAndroid;
import com.ustadmobile.port.android.impl.qr.NV21OMRImageSource;
import com.ustadmobile.port.android.impl.qr.RotatedNV21OMRImageSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.ustadmobile.core.model.AttendanceSheetImage.DEFAULT_PAGE_X_DISTANCE;
import static com.ustadmobile.core.model.AttendanceSheetImage.DEFAULT_PAGE_Y_DISTANCE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceCameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceCameraFragment extends Fragment implements View.OnClickListener, Camera.PreviewCallback, View.OnTouchListener {

    private CameraPreview mPreview;

    private Camera mCamera;

    private String mCurrentImgPath;

    private byte[] mPreviewFrameBuffer;

    private Object lockObj;

    private RectangleView mRectangleView;


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
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        Camera.Size size = c.getParameters().getPreviewSize();
        return c; // returns null if camera is unavailable
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

        mPreview = new CameraPreview(getContext(), mCamera, this);
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
        synchronized (lockObj) {
            mPreviewFrameBuffer = bytes;
        }
        /*
        try {
            lock.lock();
            mPreviewFrameBuffer = bytes;
        }finally{
            lock.unlock();
        }
        */
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                processYuvImage();
                break;
        }

        return false;
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

        try {
            //lock.lock();
            Camera.Parameters params = mCamera.getParameters();
            Camera.Size size = params.getPreviewSize();
            AttendanceSheetImage sheetImage = new AttendanceSheetImage(
                    AttendanceSheetImage.DEFAULT_PAGE_AREA_MARGIN, 297, 210,
                    new float[]{ DEFAULT_PAGE_X_DISTANCE, DEFAULT_PAGE_Y_DISTANCE,
                            DEFAULT_PAGE_X_DISTANCE, DEFAULT_PAGE_Y_DISTANCE},
                    841.8897637795275f/40f);
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
            */
        }catch(Exception e) {
            Log.e(UMLogAndroid.LOGTAG, "shisse", e);
            e.printStackTrace();
        }finally {
            Log.i(UMLogAndroid.LOGTAG, "Process time = " + procTime +
                    " pattern find time = " + patternTime +
                    " deocde time = " + decodeTime);
            //lock.unlock();
        }

    }
}
