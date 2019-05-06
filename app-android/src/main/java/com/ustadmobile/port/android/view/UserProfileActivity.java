package com.ustadmobile.port.android.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.UserProfilePresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.UserProfileView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class UserProfileActivity extends UstadBaseActivity implements UserProfileView {

    private Toolbar toolbar;
    private UserProfilePresenter mPresenter;

    private LinearLayout changePasswordLL, languageLL, logoutLL;

    private TextView languageSet;
    ImageView pictureEdit;
    ImageView personEditImage;

    int IMAGE_MAX_HEIGHT = 1024;
    int IMAGE_MAX_WIDTH = 1024;
    int IMAGE_QUALITY = 75;

    private String imagePathFromCamera;


    private static final int CAMERA_PERMISSION_REQUEST = 104;
    private static final int CAMERA_IMAGE_CAPTURE_REQUEST = 103 ;
    private static final int GALLERY_REQUEST_CODE = 105 ;

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_user_profile);

        //Toolbar:
        toolbar = findViewById(R.id.activity_user_profile_toolbar);
        toolbar.setTitle(getText(R.string.app_name));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        changePasswordLL = findViewById(R.id.activity_user_profile_change_password_ll);
        languageLL = findViewById(R.id.activity_user_profile_language_ll);
        logoutLL = findViewById(R.id.activity_user_profile_logout_ll);
        languageSet = findViewById(R.id.activity_user_profile_language_selection);
        pictureEdit = findViewById(R.id.activity_user_profile_edit);
        personEditImage = findViewById(R.id.activity_user_profile_user_image);

        //Call the Presenter
        mPresenter = new UserProfilePresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        changePasswordLL.setOnClickListener(v -> mPresenter.handleClickChangePassword());
        languageLL.setOnClickListener(v -> mPresenter.handleClickChangeLanguage());
        logoutLL.setOnClickListener(v -> handleClickLogout());

        pictureEdit.setOnClickListener(v -> showGetImageAlertDialog());
    }


    public void showGetImageAlertDialog(){

        AlertDialog.Builder adb = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Select image from Camera or Gallery")

                .setPositiveButton(R.string.camera, (dialog, which) -> {
                    addImageFromCamera();
                    dialog.dismiss();
                })

                .setNegativeButton(R.string.gallery, (dialog, which) -> {
                    addImageFromGallery();
                    dialog.dismiss();
                });

        adb.create();
        adb.show();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    @Override
    public void updateImageOnView(String imagePath){
        imagePathFromCamera = imagePath;
        File output = new File(imagePath);

        int iconDimen = dpToPx(150);

        if (output.exists()) {
            Uri profileImage = Uri.fromFile(output);

            runOnUiThread(() -> {
                Picasso
                        .get()
                        .load(profileImage)
                        .transform(new CircleTransform())
                        .resize(iconDimen, iconDimen)
                        .centerCrop()
                        .into(personEditImage);

                //Click on image - open dialog to show bigger picture
                personEditImage.setOnClickListener(view ->
                        mPresenter.openPictureDialog(imagePath));
            });

        }
    }

    @Override
    public void addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserProfileActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
            return;
        }
        startCameraIntent();
    }


    @Override
    public void addImageFromGallery(){
        //READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserProfileActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_REQUEST_CODE);
            return;
        }

        startGalleryIntent();
    }

    private void startGalleryIntent(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }



    /**
     * Starts the camera intent.
     */
    private void startCameraIntent(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = getFilesDir();
        File output = new File(dir, mPresenter.getLoggedInPersonUid() + "_image.png");
        imagePathFromCamera = output.getAbsolutePath();

        Uri cameraImage = FileProvider.getUriForFile(getApplicationContext(),
                getPackageName() + ".fileprovider", output);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImage);

        List<ResolveInfo> resInfoList =
                getPackageManager().queryIntentActivities(cameraIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, cameraImage,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE_REQUEST);
    }


    //this is how you check permission grant task result.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraIntent();
                }
                break;
            case GALLERY_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryIntent();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CAMERA_IMAGE_CAPTURE_REQUEST:

                    //Compress the image:
                    compressImage();

                    File imageFile = new File(imagePathFromCamera);
                    mPresenter.handleCompressedImage(imageFile);

                    break;

                case GALLERY_REQUEST_CODE:

                    Uri selectedImage = data.getData();


                    String picPath = doInBackground(selectedImage);
                    imagePathFromCamera = picPath;
                    if(imagePathFromCamera == null){
                        sendMessage(MessageID.unable_open_image);
                        return;
                    }

                    //Compress the image:
                    compressImage();

                    File galleryFile = new File(imagePathFromCamera);
                    mPresenter.handleCompressedImage(galleryFile);
                    break;
            }
        }
    }

    @Override
    public void sendMessage(int messageId) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String toast = impl.getString(messageId, this);
        runOnUiThread(() -> Toast.makeText(
                this,
                toast,
                Toast.LENGTH_SHORT
        ).show());
    }

    protected String doInBackground(Uri... fileUris) {
        Cursor cursor = null;
        InputStream fileIn = null;
        OutputStream tmpOut = null;
        String tmpFilePath = null;

        try {
            //As per https://developer.android.com/guide/topics/providers/document-provider
            cursor = getContentResolver().query(fileUris[0], null, null,
                    null, null, null);
            if(cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor
                        .getColumnIndex(OpenableColumns.DISPLAY_NAME));
                String extension = UMFileUtil.getExtension(displayName);

                File tmpFile = File.createTempFile("SelectedFileTmp",
                        "-"+System.currentTimeMillis() + "." + extension);
                fileIn = getContentResolver().openInputStream(fileUris[0]);
                tmpOut = new FileOutputStream(tmpFile);
                UMIOUtils.readFully(fileIn, tmpOut);
                tmpFilePath = tmpFile.getAbsolutePath();
            }else {

            }
        }catch(Exception e) {
            e.printStackTrace();

        }finally {
            if(cursor != null)
                cursor.close();

            UMIOUtils.closeQuietly(fileIn);
            UMIOUtils.closeQuietly(tmpOut);
        }

        return tmpFilePath;
    }

    /**
     * Compress the image set using Compressor.
     *
     */
    public void compressImage() {
        File imageFile = new File(imagePathFromCamera);
        try {
            Compressor c = new Compressor(this)
                    .setMaxWidth(IMAGE_MAX_WIDTH)
                    .setMaxHeight(IMAGE_MAX_HEIGHT)
                    .setQuality(IMAGE_QUALITY)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(imageFile.getPath() + "_" + imageFile.getName());

            if(imageFile.exists()){
                File compressedImageFile = c.compressToFile(imageFile);
                if(!imageFile.delete()){
                    System.out.print("Could not delete " + imagePathFromCamera);
                }
                imagePathFromCamera = compressedImageFile.getAbsolutePath();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClickLogout(){
        finishAffinity();
        mPresenter.handleClickLogout();
    }

    @Override
    public void updateToolbarTitle(String personName) {
        toolbar.setTitle(personName);
    }

    @Override
    public void setLanguageSet(String lang) {
        languageSet.setText(lang);
    }
}
