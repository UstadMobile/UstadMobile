package com.ustadmobile.port.android.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Hashtable;
import java.util.Map;
import java.util.WeakHashMap;

public class CatalogActivity extends UstadBaseActivity  {

    public static final String FRAGMENT_CATALOG_TAG = "CAT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_catalog);
        setUMToolbar(R.id.catalog_toolbar);
        setHandleUIStringsOnResume(false);
        setDirectionFromSystem();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CatalogOPDSFragment currentFrag = CatalogOPDSFragment.newInstance(getIntent().getExtras());
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.catalog_fragment_container,
                    currentFrag, FRAGMENT_CATALOG_TAG).commit();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                UstadMobileSystemImpl.getInstance().go(BasePointView.VIEW_NAME,
                        BasePointController.makeDefaultBasePointArgs(this), this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
