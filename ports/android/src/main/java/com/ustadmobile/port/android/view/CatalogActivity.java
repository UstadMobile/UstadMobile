package com.ustadmobile.port.android.view;

import android.app.ProgressDialog;
import android.net.Uri;
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
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Map;
import java.util.WeakHashMap;

public class CatalogActivity extends AppCompatActivity implements CatalogOPDSFragment.OnFragmentInteractionListener, ListView.OnItemClickListener {

    private static Map<Integer, CatalogViewAndroid> viewMap;

    private Map<CatalogViewAndroid, CatalogOPDSFragment> opdsFragmentMap;

    private ProgressDialog progressDialog;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    public static final String FRAGMENT_CATALOG_TAG = "CAT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_catalog);

        //Toolbar toolbar =
        Toolbar toolbar = (Toolbar)findViewById(R.id.catalog_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.catalog_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.catalog_left_drawer_list);

        mDrawerList.setOnItemClickListener(this);

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



        UMAndroidUtil.setDirectionIfSupported(findViewById(R.id.catalog_relativelayout),
                UstadMobileSystemImpl.getInstance().getDirection());

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


    public void setMenuOptions(String[] menuOptions) {
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawermenuitem,
                menuOptions));
    }


    @Override
    public void onStart() {
        super.onStart();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStart(this);
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void onDestroy() {
        super.onDestroy();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //someone has clicked the menu from the sidebar - direct this to fragment
        Fragment currentFrag = getSupportFragmentManager().findFragmentByTag(FRAGMENT_CATALOG_TAG);
        if(currentFrag != null) {
            ((CatalogOPDSFragment)currentFrag).handleClickMenuItem(i);
        }
    }
}
