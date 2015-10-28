package com.ustadmobile.port.android.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AttendanceController;
import com.ustadmobile.core.model.AttendanceRowModel;
import com.ustadmobile.core.view.AttendanceView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AttendanceActivity extends UstadBaseActivity implements AttendanceView {

    protected AttendanceController mController;

    public static final String TAG_STARTFRAG = "startfrag";

    public static final String TAG_RESULTSFRAG = "resultsfrag";

    public static final String TAG_CLASSLISTFRAG = "classlistfrag";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private String mCurrentImgPath;

    protected AttendanceRowModel[] mAttendanceResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        mController = AttendanceController.makeControllerForView(this);
        setBaseController(mController);
        setUMToolbar();
        setTitle("Attendance");
        if(savedInstanceState == null) {
            mController.handleStartFlow();
        }
    }

    @Override
    public void showStartPrompt() {

    }

    @Override
    public void showClassList(String[] classList) {
        Fragment classListFrag = AttendanceSelectClassListFragment.newInstance(classList);
        getSupportFragmentManager().beginTransaction().replace(R.id.attendance_fragment_container,
                classListFrag, TAG_CLASSLISTFRAG).commit();
    }


    @Override
    public void showTakePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch(IOException e) {

            }
            if(photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            int targetW = 800;
            int targetH = 1400;

            //for offline usage of a fixed image via the emulator
            //mCurrentImgPath = "/sdcard/IMG_20151025_241421713.jpg";

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentImgPath, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            if(photoW > targetW || photoH > targetH) {
                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
                bmOptions.inSampleSize = scaleFactor;
            }else {
                bmOptions.inSampleSize = 1;
            }


            bmOptions.inJustDecodeBounds = false;
            ;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentImgPath, bmOptions);
            mController.handlePictureAcquired(bitmap);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void showResult(AttendanceRowModel[] results) {
        this.mAttendanceResults = results;
        Fragment resultsFrag = AttendanceConfirmFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.attendance_fragment_container,
                resultsFrag, TAG_RESULTSFRAG).commit();
    }

    protected AttendanceRowModel[] getAttendanceResults() {
        if(this.mAttendanceResults != null) {
            return mAttendanceResults;
        }else {
            return new AttendanceRowModel[0];
        }
    }
}
