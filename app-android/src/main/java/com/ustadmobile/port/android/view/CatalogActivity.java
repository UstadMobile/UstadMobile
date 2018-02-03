package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;


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
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                impl.go(impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null, this), this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
