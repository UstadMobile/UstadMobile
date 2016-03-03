package com.ustadmobile.port.android.view;


import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.toughra.ustadmobile.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceCameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceCameraFragment extends Fragment implements View.OnClickListener {

    private CameraPreview mPreview;

    private Camera mCamera;

    private String mCurrentImgPath;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
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

        View view = inflater.inflate(R.layout.fragment_attendance_camera, container, false);
        mCamera = getCameraInstance();
        FrameLayout preview = (FrameLayout)view.findViewById(R.id.fragment_attendance_camera_preview);
        mPreview = new CameraPreview(getContext(), mCamera);

        preview.addView(mPreview, 0);

        Button captureButton = (Button)view.findViewById(R.id.fragment_attendance_camera_capture);
        captureButton.setOnClickListener(this);

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
}
