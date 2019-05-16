package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AuditLogListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.AuditLogListView;
import com.ustadmobile.lib.db.entities.AuditLogWithNames;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class AuditLogListActivity extends UstadBaseActivity implements AuditLogListView,
        PopupMenu.OnMenuItemClickListener {

    private Toolbar toolbar;
    private AuditLogListPresenter mPresenter;
    private RecyclerView mRecyclerView;


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

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_export, popup.getMenu());
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.getMenu().findItem(R.id.menu_export_xls).setVisible(false);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_export_csv) {
            mPresenter.dataToCSV();
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_audit_log_list);

        //Toolbar:
        toolbar = findViewById(R.id.activity_audit_log_list_toolbar);
        toolbar.setTitle(getText(R.string.audit_log));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_audit_log_list_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new AuditLogListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_audit_log_list_fab);

        fab.setOnClickListener(v -> showPopup(v));


    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<AuditLogWithNames> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AuditLogWithNames>() {
                @Override
                public boolean areItemsTheSame(AuditLogWithNames oldItem,
                                               AuditLogWithNames newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(AuditLogWithNames oldItem,
                                                  AuditLogWithNames newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<AuditLogWithNames> listProvider) {
        AuditLogListRecyclerAdapter recyclerAdapter =
                new AuditLogListRecyclerAdapter(DIFF_CALLBACK, mPresenter,this,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, AuditLogWithNames> factory =
                (DataSource.Factory<Integer, AuditLogWithNames>)
                        listProvider.getProvider();
        LiveData<PagedList<AuditLogWithNames>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void generateCSVReport(List<String[]> data) {

        String csvReportFilePath = "";
        //Create the file.

        File dir = getFilesDir();
        File output = new File(dir, "classbook_audit_log_" +
                System.currentTimeMillis() + ".csv");
        csvReportFilePath = output.getAbsolutePath();

        try {
            FileWriter fileWriter = new FileWriter(csvReportFilePath);
            Iterator<String[]> tableTextdataIterator = data.iterator();

            while(tableTextdataIterator.hasNext()){
                boolean firstDone = false;
                String[] lineArray = tableTextdataIterator.next();
                for(int i=0;i<lineArray.length;i++){
                    if(firstDone){
                        fileWriter.append(",");
                    }
                    firstDone = true;
                    fileWriter.append(lineArray[i]);
                }
                fileWriter.append("\n");
            }
            fileWriter.close();



        } catch (IOException e) {
            e.printStackTrace();
        }

        String applicationId = getPackageName();
        Uri sharedUri = FileProvider.getUriForFile(this,
                applicationId+".fileprovider",
                new File(csvReportFilePath));
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
    }
}
