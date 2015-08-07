package com.ustadmobile.port.android.view;

import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.util.Map;
import java.util.WeakHashMap;

public class CatalogActivity extends AppCompatActivity implements CatalogOPDSFragment.OnFragmentInteractionListener {

    private CatalogViewAndroid currentView;

    private static Map<Integer, CatalogViewAndroid> viewMap;

    private Fragment currentFrag;

    private Map<CatalogViewAndroid, CatalogOPDSFragment> opdsFragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int viewId = getIntent().getIntExtra(UstadMobileSystemImplAndroid.EXTRA_VIEWID, 0);
        UstadMobileSystemImplAndroid.getInstanceAndroid().setCurrentContext(this);
        setContentView(R.layout.activity_catalog);

        opdsFragmentMap = new WeakHashMap<CatalogViewAndroid, CatalogOPDSFragment>();

        currentView = CatalogViewAndroid.getViewById(viewId);
        currentView.setCatalogViewActivity(this);

        currentFrag = CatalogOPDSFragment.newInstance(viewId);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
            currentFrag).commit();
    }

    /**
     * When the activity is running - change the fragment showing.
     *
     * @param view
     */
    public void setCurrentOPDSCatalogFragment(CatalogViewAndroid view) {
        CatalogOPDSFragment fragment = opdsFragmentMap.get(view);
        if(fragment == null) {
            fragment = CatalogOPDSFragment.newInstance(view.getViewId());
            opdsFragmentMap.put(view, fragment);
        }

        String backEntryTitle = view.getController().getModel().opdsFeed.title;
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();

        fTransaction.replace(R.id.fragment_container, fragment);
        fTransaction.addToBackStack(backEntryTitle);
        fTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fTransaction.commit();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(Uri uri) {

    }
}
