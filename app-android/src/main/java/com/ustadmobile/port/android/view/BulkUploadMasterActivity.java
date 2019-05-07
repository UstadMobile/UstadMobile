package com.ustadmobile.port.android.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.BulkUploadMasterPresenter;
import com.ustadmobile.core.view.BulkUploadMasterView;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.android.util.UmAndroidUriUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class BulkUploadMasterActivity extends UstadBaseActivity implements BulkUploadMasterView {

    private String filePathFromFilePicker;
    private BulkUploadMasterPresenter mPresenter;
    private ProgressBar mProgressBar;
    private FloatingTextButton fab;
    private Button selectFileButton;
    private Spinner timeZoneSpinner;

    private static final int FILE_PERMISSION_REQUEST = 400;

    private TextView heading;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_bulk_upload_master);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.activity_bulk_upload_master_toolbar);
        toolbar.setTitle(R.string.bulk_upload_master);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Timezone spinner
        timeZoneSpinner = findViewById(R.id.activity_bulk_upload_master_timezone_spinner);
        timeZoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.setTimeZoneSelected(position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Call the presenter
        mPresenter = new BulkUploadMasterPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Button
        selectFileButton = findViewById(R.id.activity_bulk_upload_master_upload_button);
        selectFileButton.setOnClickListener(v -> chooseFileFromDevice());

        //Heading TextView
        heading = findViewById(R.id.activity_bulk_upload_select_file_text);

        //Progress bar
        mProgressBar = findViewById(R.id.activity_bulk_upload_master_progressbar);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setScaleY(3f);

        //FAB
        fab = findViewById(R.id.activity_bulk_upload_master_fab);
        fab.setOnClickListener(v -> parseFile(filePathFromFilePicker));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setInProgress(boolean inProgress) {
        mProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        fab.setEnabled(!inProgress);
        fab.setVisibility(inProgress?View.INVISIBLE:View.VISIBLE);
        selectFileButton.setEnabled(inProgress);
        selectFileButton.setVisibility(inProgress?View.INVISIBLE:View.VISIBLE);
        selectFileButton.getBackground().setAlpha(inProgress ? 128 : 255 );
    }

    @Override
    public void updateProgressValue(int line, int nlines) {
        runOnUiThread(() -> {
            int value = (line*100/nlines);
            mProgressBar.setProgress(value);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == FILE_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted - call the method here
                chooseFileFromDevice();
            } else {
                // permission denied, you may keep on requesting it or just finish the activity
            }
            return;
        }
    }

    @Override
    public void setTimeZonesList(List<String> timeZoneIds) {

        ArrayAdapter<String> timeZoneAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, timeZoneIds);
        timeZoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        timeZoneSpinner.setAdapter(timeZoneAdapter);

        int index = timeZoneIds.indexOf(TimeZone.getDefault().getID());
        timeZoneSpinner.setSelection(index);

    }

    @Override
    public void chooseFileFromDevice() {

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(BulkUploadMasterActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    FILE_PERMISSION_REQUEST);
            return;
        }

        String[] mimeTypes ={"text/*"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        } else {
            StringBuilder mimeTypesStr = new StringBuilder();
            for (String mimeType : mimeTypes) {
                mimeTypesStr.append(mimeType).append("|");
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"Select CSV File"),
                FILE_PERMISSION_REQUEST);
    }

    @Override
    public void parseFile(String filePath) {
        setInProgress(true);
        if(filePath == null || filePath.isEmpty()){
            showMessage(getText(R.string.select_file).toString());
        }else {
            File sourceFile = new File(filePath);
            readFile(sourceFile);
        }
    }


    public void readFile(File sourceFile){
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            mPresenter.setLines(lines);
            mPresenter.setCurrentPosition(0); //skip first line
            mPresenter.processNextLine();

        } catch (FileNotFoundException e) {
            showMessage("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            showMessage("Unable to process the file");
            e.printStackTrace();
        }
    }

    @Override
    public void showMessage(String message) {
        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT
        ).show());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case FILE_PERMISSION_REQUEST:
                    Uri selectedUri = data.getData();
                    File sourceFile = new File(Objects.requireNonNull(
                            UmAndroidUriUtil.getPath(this, selectedUri)));
                    //Do something with your file
                    filePathFromFilePicker = sourceFile.getAbsolutePath();
                    String fileSelectedString = getText(R.string.file_selected) + " " +
                            sourceFile.getName();
                    heading.setText(fileSelectedString);
                    break;
            }
        }
    }

}
