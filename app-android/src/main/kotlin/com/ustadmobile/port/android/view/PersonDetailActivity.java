package com.ustadmobile.port.android.view;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.PersonEditView.IMAGE_MAX_HEIGHT;
import static com.ustadmobile.core.view.PersonEditView.IMAGE_MAX_WIDTH;
import static com.ustadmobile.core.view.PersonEditView.IMAGE_QUALITY;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_DATE;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_DROPDOWN;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_FIELD;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_HEADER;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_PHONE_NUMBER;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_TEXT;
import static com.ustadmobile.port.android.view.ClazzEditActivity.dpToPx;
import static com.ustadmobile.port.android.view.PersonEditActivity.ADD_PERSON_ICON;

/**
 * The PersonDetail activity - Shows the detail of a person (like a contact in an address book)
 *
 * This Activity extends UstadBaseActivity and implements PersonDetailView
 */
public class PersonDetailActivity extends UstadBaseActivity implements PersonDetailView {

    private static final int CAMERA_PERMISSION_REQUEST = 104;
    private LinearLayout mLinearLayout;

    private RecyclerView mRecyclerView;

    private PersonDetailPresenter mPresenter;
    ImageView personEditImage;
    private FloatingTextButton fab;
    Button updateImageButton;
    private String imagePathFromCamera;
    private static final int CAMERA_IMAGE_CAPTURE_REQUEST = 103 ;

    private Menu mOptionsMenu;

    public static final String CALL_ICON_NAME = "ic_call_bcd4_24dp";
    public static final String TEXT_ICON_NAME = "ic_textsms_bcd4_24dp";

    private LinearLayout enrollInClassLL, recordDropoutLL;

    LinearLayout customFieldsLL;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (i == R.id.update_username_password) {
            mPresenter.goToUpdateUsernamePassword();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_person_detail, menu);
        mOptionsMenu = menu;
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_person_detail);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.activity_person_detail_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mLinearLayout = findViewById(R.id.activity_person_detail_fields_linear_layout);

        //FAB
        fab = findViewById(R.id.activity_person_detail_fab_edit);

        //Load the Image
        personEditImage = findViewById(R.id.activity_person_detail_student_image);

        //Update image button
        updateImageButton = findViewById(R.id.activity_person_detail_student_image_button2);

        updateImageButton.setOnClickListener(view -> addImageFromCamera());

        enrollInClassLL = findViewById(R.id.activity_person_detail_action_ll_enroll_in_class_ll);
        recordDropoutLL = findViewById(R.id.activity_person_detail_action_ll_record_dropout_ll);

        customFieldsLL = findViewById(R.id.activity_person_detail_custom_fields_ll);

        //Call the Presenter
        mPresenter = new PersonDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        fab.setOnClickListener(v -> mPresenter.handleClickEdit());

        TextView callParentTextView = findViewById(R.id.activity_person_detail_action_call_parent_text);
        TextView textParentTextView = findViewById(R.id.activity_person_detail_action_text_parent_text);
        ImageView callParentImageView =
                findViewById(R.id.activity_person_detail_action_call_parent_icon);
        ImageView textParentImageView =
                findViewById(R.id.activity_person_detail_action_text_parent_icon);
        TextView enrollInClassTextView =
                findViewById(R.id.activity_person_detail_action_enroll_in_class_text);
        ImageView enrollInClassImageView =
                findViewById(R.id.activity_person_detail_action_enroll_in_class_icon);

        callParentImageView.setOnClickListener(v -> mPresenter.handleClickCallParent());
        callParentTextView.setOnClickListener(v -> mPresenter.handleClickCallParent());

        textParentImageView.setOnClickListener(v -> mPresenter.handleClickTextParent());
        textParentTextView.setOnClickListener(v -> mPresenter.handleClickTextParent());

        enrollInClassImageView.setOnClickListener(v -> mPresenter.handleClickEnrollInClass());
        enrollInClassTextView.setOnClickListener(v -> mPresenter.handleClickEnrollInClass());

        recordDropoutLL.setOnClickListener(v -> mPresenter.handleClickRecordDropout());

    }

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void clearAllFields() {
        runOnUiThread(() -> mLinearLayout.removeAllViews());

    }

    @Override
    public void showFAB(boolean show) {
        runOnUiThread(() -> {
            fab.setEnabled(show);
            fab.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        });
    }

    @Override
    public void addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PersonDetailActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
            return;
        }
        startCameraIntent();
    }

    @Override
    public void showEnrollInClass(boolean show) {
        runOnUiThread(() -> enrollInClassLL.setVisibility(show?View.VISIBLE:View.GONE));

    }

    @Override
    public void showDropout(boolean show) {
        runOnUiThread(() -> recordDropoutLL.setVisibility(show?View.VISIBLE:View.GONE));

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
        }
    }

    /**
     * Starts the camera intent.
     */
    private void startCameraIntent(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = getFilesDir();
        File output = new File(dir, mPresenter.getPersonUid() + "_image.png");
        imagePathFromCamera = output.getAbsolutePath();

        Uri cameraImage = FileProvider.getUriForFile(this,
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
            }
        }
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

            File compressedImageFile = c.compressToFile(imageFile);
            if(!imageFile.delete()){
                System.out.print("Could not delete " + imagePathFromCamera);
            }
            imagePathFromCamera = compressedImageFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showUpdateImageButton(boolean show) {
        runOnUiThread(() -> {
            updateImageButton.setEnabled(show);
            updateImageButton.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        });
    }

    @Override
    public void updateImageOnView(String imagePath){
        File output = new File(imagePath);

        if (output.exists()) {
            Uri profileImage = Uri.fromFile(output);

            runOnUiThread(() -> {
                Picasso
                        .get()
                        .load(profileImage)
                        .fit()
                        .centerCrop()
                        .into(personEditImage);

                //Click on image - open dialog to show bigger picture
                personEditImage.setOnClickListener(view ->
                        mPresenter.openPictureDialog(imagePath));
            });

        }
    }

    @Override
    public void setField(int index, PersonDetailViewField field, Object value) {
        if(value == null){
            value = "";
        }
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String label = null;
        if(field.getMessageLabel() != 0) {
            label = impl.getString(field.getMessageLabel(), getContext());
        }

        switch(field.getFieldType()){
            case FIELD_TYPE_HEADER:

                //Add The Divider
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                ));
                divider.setBackgroundColor(Color.parseColor("#B3B3B3"));
                mLinearLayout.addView(divider);

                //Add the Header
                TextView header = new TextView(this);
                assert label != null;
                header.setText(label.toUpperCase());
                header.setTextSize(12);
                header.setPadding(16,0,0,2);
                mLinearLayout.addView(header);

                if(field.getMessageLabel() == MessageID.classes){
                    //Add a recyclerview of classes
                    mRecyclerView = new RecyclerView(this);

                    RecyclerView.LayoutManager mRecyclerLayoutManager =
                            new LinearLayoutManager(getApplicationContext());
                    mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

                    //Add the layout
                    mLinearLayout.addView(mRecyclerView);

                    //Generate the live data and set it
                    mPresenter.generateAssignedClazzesLiveData();
                }

                break;
            case FIELD_TYPE_TEXT:
            case FIELD_TYPE_FIELD:

                int messageLabel = field.getMessageLabel();
                //If this is just the full name, set it and continue
                if(messageLabel == MessageID.field_fullname){
                    TextView name = findViewById(R.id.activity_person_detail_student_name);
                    name.setText(value.toString());
                    break;
                }

                LinearLayout hll = new LinearLayout(this);
                hll.setOrientation(LinearLayout.HORIZONTAL);
                hll.setPadding(16,16,16,16);


                String iconName = field.getIconName();

                if(iconName == null || iconName.length() == 0){
                    iconName = ADD_PERSON_ICON;
                }

                int iconResId = getResourceId(iconName, "drawable", getPackageName());
                AppCompatImageView icon = new AppCompatImageView(this);
                icon.setImageResource(iconResId);
                if(iconName.equals(ADD_PERSON_ICON)) icon.setAlpha(0);
                icon.setPadding(16,0,4,0);
                hll.addView(icon);


                LinearLayout vll = new LinearLayout(this);
                vll.setOrientation(LinearLayout.VERTICAL);
                vll.setPadding(16,0,0,0);

                TextView fieldValue = new TextView(this);
                if(value.toString() == ""){
                    value = "-";
                }
                fieldValue.setText(value.toString());
                fieldValue.setPadding(16,4,4,0);
                vll.addView(fieldValue);

                if (label != null) {
                    TextView fieldLabel = new TextView(this);
                    fieldLabel.setTextSize(10);
                    fieldLabel.setText(label);
                    fieldLabel.setPadding(16, 0, 4, 4);
                    vll.addView(fieldLabel);
                }



                //Add call and text buttons to father and mother detail
                if(field.getActionParam() != null && field.getActionParam().length() > 0){
                    AppCompatImageView textIcon = new AppCompatImageView(this);
                    textIcon.setImageResource(getResourceId(TEXT_ICON_NAME,
                            "drawable", getPackageName()));
                    //textIcon.setPadding(8,16, 32,16);
                    textIcon.setOnClickListener(v -> mPresenter.handleClickText(field.getActionParam()));

                    AppCompatImageView callIcon = new AppCompatImageView(this);
                    callIcon.setImageResource(getResourceId(CALL_ICON_NAME,
                            "drawable", getPackageName()));
                    callIcon.setPadding(32,0, 0,0);
                    callIcon.setOnClickListener(v -> mPresenter.handleClickCall(field.getActionParam()));

                    LinearLayout.LayoutParams heavyLayout = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1.0f
                    );
                    View fillIt = new View(this);
                    fillIt.setLayoutParams(heavyLayout);


                    vll.setLayoutParams(heavyLayout);
                    hll.addView(vll);

                    LinearLayout talkToMe = new LinearLayout(this);
                    talkToMe.setOrientation(LinearLayout.HORIZONTAL);
                    talkToMe.setGravity(Gravity.RIGHT);
                    talkToMe.setWeightSum(2);
                    textIcon.setLayoutParams(heavyLayout);
                    callIcon.setLayoutParams(heavyLayout);
                    talkToMe.addView(textIcon);
                    talkToMe.addView(callIcon);
                    hll.addView(talkToMe);

                }else{
                    hll.addView(vll);
                }

                mLinearLayout.addView(hll);

                break;
            case FIELD_TYPE_DROPDOWN:
                break;
            case FIELD_TYPE_PHONE_NUMBER:
                break;
            case FIELD_TYPE_DATE:
                break;
            default:
                break;
        }

    }

    @Override
    public void addComponent(String value, String label) {

        LinearLayout hll = new LinearLayout(this);
        hll.setOrientation(LinearLayout.HORIZONTAL);
        hll.setPadding(16,16,16,16);


        String iconName = ADD_PERSON_ICON;

        int iconResId = getResourceId(iconName, "drawable", getPackageName());
        AppCompatImageView icon = new AppCompatImageView(this);
        icon.setImageResource(iconResId);
        if(iconName.equals(ADD_PERSON_ICON)) icon.setAlpha(0);
        icon.setPadding(16,0,4,0);
        hll.addView(icon);


        LinearLayout vll = new LinearLayout(this);
        vll.setOrientation(LinearLayout.VERTICAL);
        vll.setPadding(16,0,0,0);

        TextView fieldValue = new TextView(this);
        if(value.toString() == ""){
            value = "-";
        }
        fieldValue.setText(value.toString());
        fieldValue.setPadding(16,4,4,0);
        vll.addView(fieldValue);

        if (label != null) {
            TextView fieldLabel = new TextView(this);
            fieldLabel.setTextSize(10);
            fieldLabel.setText(label);
            fieldLabel.setPadding(16, 0, 4, 4);
            vll.addView(fieldLabel);
        }

        hll.addView(vll);

        customFieldsLL.addView(hll);

    }

    @Override
    public void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider) {

        SimpleClazzListRecyclerAdapter recyclerAdapter =
                new SimpleClazzListRecyclerAdapter(DIFF_CALLBACK, getApplicationContext());
        // A warning is expected
        DataSource.Factory<Integer, ClazzWithNumStudents> factory =
                (DataSource.Factory<Integer, ClazzWithNumStudents>) clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithNumStudents>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void handleClickCall(String number) {
        startActivity(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + number)));
    }

    @Override
    public void handleClickText(String number) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",
                number, null)));
    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<ClazzWithNumStudents> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ClazzWithNumStudents>() {
                @Override
                public boolean areItemsTheSame(ClazzWithNumStudents oldItem,
                                               ClazzWithNumStudents newItem) {
                    return oldItem.getClazzUid() ==
                            newItem.getClazzUid();
                }

                @Override
                public boolean areContentsTheSame(ClazzWithNumStudents oldItem,
                                                  ClazzWithNumStudents newItem) {
                    return oldItem.equals(newItem);
                }
            };


    @Override
    public void addCustomFieldText(CustomField label, String value) {
        //customFieldsLL

        //Calculate the width of the screen.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;

        //The field is an input type. So we are gonna add a TextInputLayout:
        TextInputLayout fieldTextInputLayout = new TextInputLayout(this);
        int viewId = View.generateViewId();
        mPresenter.addToMap(viewId, label.getCustomFieldUid());
        fieldTextInputLayout.setId(viewId);
        //Edit Text is inside a TextInputLayout
        TextInputLayout.LayoutParams textInputLayoutParams =
                new TextInputLayout.LayoutParams(displayWidth,
                        TextInputLayout.LayoutParams.MATCH_PARENT);

        int widthWithPadding = displayWidth - Companion.dpToPx(28);
        //The EditText
        EditText fieldEditText = new EditText(this);
        fieldEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        ViewGroup.LayoutParams editTextParams =
                new LinearLayout.LayoutParams(
                        widthWithPadding,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        fieldEditText.setLayoutParams(editTextParams);
        fieldEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        fieldEditText.setText(value);

        fieldEditText.setHint(label.getCustomFieldName());

        fieldTextInputLayout.addView(fieldEditText, textInputLayoutParams);
        fieldTextInputLayout.setPadding(Companion.dpToPx(8),0,0,0);
        customFieldsLL.addView(fieldTextInputLayout);
    }

    @Override
    public void addCustomFieldDropdown(CustomField label, String[] options, int selected) {
        //Calculate the width of the screen.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;

        //The field is an input type. So we are gonna add a TextInputLayout:
        //Edit Text is inside a TextInputLayout
        TextInputLayout.LayoutParams textInputLayoutParams =
                new TextInputLayout.LayoutParams(displayWidth,
                        TextInputLayout.LayoutParams.MATCH_PARENT);

        int widthWithPadding = displayWidth - Companion.dpToPx(28);

        //Spinner time
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                        options);
        spinner.setAdapter(spinnerArrayAdapter);

        //Spinner label
        TextView labelTV = new TextView(this);
        labelTV.setText(label.getCustomFieldName());

        int viewId = View.generateViewId();
        mPresenter.addToMap(viewId, label.getCustomFieldUid());

        //VLL
        LinearLayout vll = new LinearLayout(this);
        vll.setId(viewId);
        vll.setLayoutParams(textInputLayoutParams)  ;
        vll.setOrientation(LinearLayout.VERTICAL);

        vll.addView(labelTV);
        vll.addView(spinner);

        vll.setPadding(Companion.dpToPx(8),0,0,0);
        customFieldsLL.addView(vll);
    }

    @Override
    public void clearAllCustomFields() {
        customFieldsLL.removeAllViews();
    }


}
