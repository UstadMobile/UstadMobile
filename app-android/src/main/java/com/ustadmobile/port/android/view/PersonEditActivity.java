package com.ustadmobile.port.android.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonEditPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.port.android.generated.MessageIDMap;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import id.zelory.compressor.Compressor;

import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DATE;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DROPDOWN;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_FIELD;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_HEADER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_TEXT;

public class PersonEditActivity extends UstadBaseActivity implements PersonEditView {

    private Toolbar toolbar;
    private LinearLayout mLinearLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private PersonEditPresenter mPresenter;

    private String imagePathFromCamera;

    public static final int DEFAULT_PADDING = 16;
    public static final int DEFAULT_PADDING_HEADER_BOTTOM = 16;
    public static final int DEFAULT_DIVIDER_HEIGHT = 2;
    public static final int DEFAULT_TEXT_PADDING_RIGHT = 4;
    public static final String BLANK_ICON = "ic_none_24dp";
    public static final String ADD_PERSON_ICON = "ic_person_add_black_24dp";
    public static final int HEADER_TEXT_SIZE = 12;
    public static final int LABEL_TEXT_SIZE = 10;
    public static final String COLOR_GREY= "#B3B3B3";

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int CAMERA_IMAGE_CAPTURE_REQUEST = 101 ;

    ImageView personEditImage;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout:
        setContentView(R.layout.activity_person_edit);

        //Toolbar
        toolbar = findViewById(R.id.activity_person_edit_toolbar);
        toolbar.setTitle(getText(R.string.edit_person));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the header & fields layout
        mLinearLayout = findViewById(R.id.activity_person_edit_fields_linear_layout);

        //Call the presenter
        mPresenter = new PersonEditPresenter(this, UMAndroidUtil.bundleToHashtable(
                getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        personEditImage = findViewById(R.id.activity_person_edit_student_image);
        personEditImage.setOnClickListener(v -> addImageFromCamera());

        Button personEditImageButton = findViewById(R.id.activity_person_edit_student_image_button);
        personEditImageButton.setOnClickListener(v -> addImageFromCamera());


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
        mLinearLayout.removeAllViews();
    }

    public void setEditField(long fieldUid, int fieldType, String label, int labelId,
                             String iconName, boolean editMode,
                             LinearLayout thisLinearLayout, Object thisValue){


        //Set icon if not present (for margins to align ok)
        if(iconName == null || iconName.length() == 0){
            iconName = ADD_PERSON_ICON;
        }

        LinearLayout.LayoutParams dividerLayout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        DEFAULT_DIVIDER_HEIGHT);

        LinearLayout.LayoutParams parentParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        ViewGroup.LayoutParams editTextParams =
                new LinearLayout.LayoutParams(
                        width,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        TextInputLayout.LayoutParams tilp = new TextInputLayout.LayoutParams(width,
                TextInputLayout.LayoutParams.MATCH_PARENT);

        LinearLayout hll = new LinearLayout(this);
        hll.setLayoutParams(parentParams);
        hll.setOrientation(LinearLayout.HORIZONTAL);

        TextInputLayout til = new TextInputLayout(this);

        View editView = null;

        switch(fieldType) {

            case FIELD_TYPE_HEADER:
                //Add The Divider
                View divider = new View(this);
                divider.setLayoutParams(dividerLayout);
                divider.setBackgroundColor(Color.parseColor(COLOR_GREY));
                thisLinearLayout.addView(divider);

                //Add the Header
                TextView header = new TextView(this);
                header.setText(label.toUpperCase());
                header.setTextSize(HEADER_TEXT_SIZE);
                header.setPadding(DEFAULT_PADDING, 0, 0, DEFAULT_PADDING_HEADER_BOTTOM);
                thisLinearLayout.addView(header);

                //Add for classes
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                if(label.equals(impl.getString(MessageID.classes, getContext()))){

                    //Add Add new Class button
                    LinearLayout addPersonToClazzHL = new LinearLayout(this);
                    addPersonToClazzHL.setLayoutParams(parentParams);
                    addPersonToClazzHL.setOrientation(LinearLayout.HORIZONTAL);

                    //Add the icon
                    int addIconResId = getResourceId(ADD_PERSON_ICON,
                            "drawable", getPackageName());
                    ImageView addIcon = new ImageView(this);
                    addIcon.setImageResource(addIconResId);
                    addIcon.setPadding(DEFAULT_PADDING,0,DEFAULT_TEXT_PADDING_RIGHT,0);
                    addPersonToClazzHL.addView(addIcon);

                    //Add the button
                    Button addPersonButton = new Button(this);
                    addPersonButton.setIncludeFontPadding(false);
                    addPersonButton.setMinHeight(0);
                    //addPersonButton.setLayoutParams(buttonLayout);
                    addPersonButton.setText(impl.getString(MessageID.add_person_to_class,
                            getContext()));
                    addPersonButton.setBackground(null);
                    addPersonButton.setPadding(DEFAULT_PADDING, 0, 0, 0);
                    addPersonButton.setOnClickListener(v -> mPresenter.handleClickAddNewClazz());
                    addPersonToClazzHL.addView(addPersonButton);

                    mLinearLayout.addView(addPersonToClazzHL);

                    //Add a recyclerview of classes
                    mRecyclerView = new RecyclerView(this);

                    mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
                    mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

                    //Add the layout
                    mLinearLayout.addView(mRecyclerView);

                    //Generate the live data and set it
                    mPresenter.generateAssignedClazzesLiveData();
                }

                break;

            case FIELD_TYPE_TEXT:
            case FIELD_TYPE_FIELD:
            case FIELD_TYPE_PHONE_NUMBER:
            case FIELD_TYPE_DATE:

                //Add the icon
                int iconResId = getResourceId(iconName, "drawable", getPackageName());
                ImageView icon = new ImageView(this);
                if(iconName.equals(ADD_PERSON_ICON)){
                    icon.setAlpha(0);
                }
                icon.setImageResource(iconResId);
                icon.setPadding(DEFAULT_PADDING,0,DEFAULT_TEXT_PADDING_RIGHT,0);
                hll.addView(icon);

                EditText et = new EditText(this);

                et.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                et.setLayoutParams(editTextParams);
                if (label != null) {
                    et.setHint(label);
                }
                if (thisValue != null) {
                    et.setText(thisValue.toString());
                }
                if (fieldType == FIELD_TYPE_PHONE_NUMBER) {
                    et.setInputType(InputType.TYPE_CLASS_PHONE);

                }
                if (fieldType == FIELD_TYPE_DATE) {
                    //et.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);

                    Calendar myCalendar = Calendar.getInstance();

                    DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        et.setText(UMCalendarUtil.getPrettyDateFromLong(myCalendar.getTimeInMillis()));

                    };

                    et.setFocusable(false);

                    et.setOnClickListener(v -> new DatePickerDialog(
                            PersonEditActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show());


                }
                if (fieldType == FIELD_TYPE_TEXT) {
                    et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }

                if(fieldType != FIELD_TYPE_DATE){
                    et.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            mPresenter.handleFieldEdited(fieldUid, s.toString());
                        }
                    });
                }

                til.addView(et, tilp);

                editView = til;
                //End of TEXT

                break;
            case FIELD_TYPE_DROPDOWN:
                break;

            default:
                break;
        }

        if(editView != null) {
            hll.addView(editView);
        }

        mLinearLayout.addView(hll);

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_done, menu);
        return true;
    }

    //Handling Action Bar button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        //If this activity started from other activity
        if (i == R.id.menu_catalog_entry_presenter_share) {
            mPresenter.handleClickDone();

            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setField(int index, long fieldUid, PersonDetailViewField field, Object value) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String label = null;
        int labelId = 0;
        if(field.getMessageLabel() != 0) {
            label = impl.getString(field.getMessageLabel(), getContext());
            labelId = MessageIDMap.ID_MAP.get(field.getMessageLabel());
        }

        setEditField(fieldUid, field.getFieldType(), label, labelId,field.getIconName(),
                true, mLinearLayout, value);

    }

    @Override
    public void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider) {
        ClazzListRecyclerAdapter recyclerAdapter =
                new ClazzListRecyclerAdapter(DIFF_CALLBACK);
        DataSource.Factory<Integer, ClazzWithNumStudents> factory =
                (DataSource.Factory<Integer, ClazzWithNumStudents>)
                        clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithNumStudents>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void updateToolbarTitle(String titleName) {
        toolbar.setTitle(titleName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void startCameraIntent(){
        String imageId = String.valueOf(System.currentTimeMillis());
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File output = new File(dir,imageId+"_image.png");
        imagePathFromCamera = output.getAbsolutePath();
        Uri cameraImage = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", output);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImage);
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
        }
    }

    /*Since most camera capture inverted
    images then you might want to rotate it first and get it as bitmap*/

    public Bitmap getCompressedImage(int quality){
        Bitmap bmp = BitmapFactory.decodeFile(imagePathFromCamera);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix,
                true);
    }

    public void compressImage() {
        File imageFile = new File(imagePathFromCamera);
        try {
            Compressor c = new Compressor(this);
            c.setDestinationDirectoryPath(imageFile.getPath() + "_" + imageFile.getName() );

            File compressedImageFile = c.compressToFile(imageFile);
            imageFile.delete();
            imagePathFromCamera = compressedImageFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateImageOnView(String imagePath){
        Uri profileImage = Uri.fromFile(new File(imagePath));

        Picasso.with(getApplicationContext()).load(profileImage).into(personEditImage);

        File profilePic = new File(imagePath);
        Picasso.with(getApplicationContext()).load(profilePic).into(personEditImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CAMERA_IMAGE_CAPTURE_REQUEST:

                    //Copress the image:
                    compressImage();

                    //set imagePathFromCamera to Person.
                    updateImageOnView(imagePathFromCamera);
                    mPresenter.updatePersonPic(imagePathFromCamera);

                    break;
            }
        }
    }

    @Override
    public void addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PersonEditActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST);
            return;
        }
        startCameraIntent();
    }


    /**
     * The Recycler Adapter
     */
    protected class ClazzListRecyclerAdapter
            extends PagedListAdapter<ClazzWithNumStudents,
                                    ClazzListRecyclerAdapter.ClazzLogDetailViewHolder> {

        protected class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
            protected ClazzLogDetailViewHolder(View itemView){
                super(itemView);
            }
        }

        protected ClazzListRecyclerAdapter(
                @NonNull DiffUtil.ItemCallback<ClazzWithNumStudents> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public ClazzLogDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

            View clazzLogDetailListItem =
                    LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.item_clazzlist_clazz_simple, parent, false);
            return new ClazzLogDetailViewHolder(clazzLogDetailListItem);
        }

        /**
         * This method sets the elements after it has been obtained for that item'th position.
         *
         * Every item in the recycler view will have set its colors if no attendance status is set.
         * every attendance button will have it-self mapped to tints on activation.
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(@NonNull ClazzLogDetailViewHolder holder, int position){
            ClazzWithNumStudents thisClazz = getItem(position);

            ((TextView)holder.itemView.findViewById(R.id.item_clazzlist_clazz_simple_clazz_name))
                    .setText(thisClazz.getClazzName());

        }
    }

    // Diff callback.
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
}
