package com.ustadmobile.port.android.view;

import android.app.ProgressDialog;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ListView;

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

    private ProgressDialog progressDialog;

    private int viewId;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewId = getIntent().getIntExtra(UstadMobileSystemImplAndroid.EXTRA_VIEWID, 0);
        setContentView(R.layout.activity_catalog);


        //Toolbar toolbar =
        Toolbar toolbar = (Toolbar)findViewById(R.id.catalog_toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setTitle("Catalog");
        //toolbar.setLogo(R.drawable.ic_launcher);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.catalog_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.catalog_left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this,//host activity
                mDrawerLayout, //DrawerLayout object
                toolbar, //
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.syncState();




        mDrawerLayout.setDrawerListener(mDrawerToggle);

        opdsFragmentMap = new WeakHashMap<CatalogViewAndroid, CatalogOPDSFragment>();
        currentFrag = CatalogOPDSFragment.newInstance(viewId);
        getSupportFragmentManager().beginTransaction().add(R.id.catalog_fragment_container,
            currentFrag).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStart(this);
        currentView = CatalogViewAndroid.getViewById(viewId);
        currentView.setCatalogViewActivity(this);
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void onDestroy() {
        super.onDestroy();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }

    /**
     * When the activity is running - change the fragment showing.
     *
     * @param view
     */
    public void setCurrentOPDSCatalogFragment(final CatalogViewAndroid view) {
        runOnUiThread(new Runnable() {
            public void run() {
                CatalogOPDSFragment fragment = opdsFragmentMap.get(view);
                if(fragment == null) {
                    fragment = CatalogOPDSFragment.newInstance(view.getViewId());
                    opdsFragmentMap.put(view, fragment);
                }

                String backEntryTitle = view.getController().getModel().opdsFeed.title;
                FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();

                fTransaction.replace(R.id.catalog_fragment_container, fragment);
                fTransaction.addToBackStack(backEntryTitle);
                fTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fTransaction.commit();
                currentFrag = fragment;
            }
        });

    }

    public Fragment getCurrentFragment() {
        return currentFrag;
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
