package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SettingsDataSyncListController;
import com.ustadmobile.core.view.SettingsDataSyncListView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsDataSyncListActivity extends UstadBaseActivity implements
        SettingsDataSyncListView, View.OnClickListener{

    private SettingsDataSyncListController mController;

    ArrayAdapter<String> syncListAdapter;

    ArrayList<String> syncListArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_data_sync_list);
        mController = new SettingsDataSyncListController(this);
        setUMToolbar(R.id.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView)findViewById(R.id.toolbarTitle)).setText("Sync List");
        Toolbar thisToolbar = (Toolbar)findViewById(R.id.toolbar);

        setTitle("Sync List");
        //Draw the sync list here

        LinkedHashMap<String, String> syncHistory =
                mController.getMainNodeSyncHistory(getContext());

        ListView syncListView = (ListView) findViewById(R.id.synclist_list);
        Iterator<Map.Entry<String, String>> syncHistoryIterator = syncHistory.entrySet().iterator();
        while(syncHistoryIterator.hasNext()){
            Map.Entry<String, String> thisEntry = syncHistoryIterator.next();
            String thisDate = String.valueOf(thisEntry.getKey());
            Long thisLongDate = Long.parseLong(thisDate);
            String thisPrettyDate = convertTime(thisLongDate);
            String thisResult = thisEntry.getValue().toString();
            String fullText = thisPrettyDate + ", Result : " + thisResult;
            syncListArray.add(fullText);
        }

        syncListAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1,
                syncListArray);
        syncListView.setAdapter(syncListAdapter);
        mController.setView(this);
    }

    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_data_p2p_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //On Option selected handling here
        return true;
    }

    @Override
    public void onClick(View view) {
        //Click handle here
    }
}
