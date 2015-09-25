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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.U;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CatalogModel;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.view.ViewFactory;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

public class CatalogActivity extends AppCompatActivity implements CatalogOPDSFragment.OnFragmentInteractionListener, ListView.OnItemClickListener, ControllerReadyListener {

    private CatalogViewAndroid currentView;

    private static Map<Integer, CatalogViewAndroid> viewMap;

    private Map<CatalogViewAndroid, CatalogOPDSFragment> opdsFragmentMap;

    private ProgressDialog progressDialog;

    private int viewId;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private String[] drawerMenuItems;

    private ActionBarDrawerToggle mDrawerToggle;

    public static String EXTRA_CATALOGURL = "CATALOGURL";

    public static String EXTRA_RESMODE = "RESOURCEMODE";

    public static final String FRAGMENT_CATALOG = "CAT";

    private Bundle mSavedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UstadMobileSystemImplAndroid.handleActivityCreate(this, savedInstanceState);

        super.onCreate(savedInstanceState);
        viewId = getIntent().getIntExtra(UstadMobileSystemImplAndroid.EXTRA_VIEWID,
            savedInstanceState != null ? savedInstanceState.getInt(UstadMobileSystemImplAndroid.EXTRA_VIEWID) : 0);
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

        currentView = CatalogViewAndroid.getViewById(viewId);
        opdsFragmentMap = new WeakHashMap<CatalogViewAndroid, CatalogOPDSFragment>();
        if(currentView != null) {
            setupFromCatalogView(currentView, savedInstanceState);
        }else {
            mSavedInstanceState = savedInstanceState;
            String catalogURL = savedInstanceState != null && savedInstanceState.getString(EXTRA_CATALOGURL) != null ?
                savedInstanceState.getString(EXTRA_CATALOGURL) : getIntent().getStringExtra(EXTRA_CATALOGURL);
            int resourceMode = savedInstanceState != null && savedInstanceState.getInt(EXTRA_RESMODE, -1) != -1 ?
                    savedInstanceState.getInt(EXTRA_RESMODE) : getIntent().getIntExtra(EXTRA_RESMODE, 1);
            loadCatalog(catalogURL, resourceMode);
        }


        UMAndroidUtil.setDirectionIfSupported(findViewById(R.id.catalog_relativelayout),
                UstadMobileSystemImpl.getInstance().getDirection());
    }

    public void loadCatalog(final String url, int resourceMode) {
        currentView = (CatalogViewAndroid) ViewFactory.makeCatalogView();
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        final int fetchFlags = CatalogController.CACHE_ENABLED;

        CatalogController.makeControllerForView(currentView, url, impl, resourceMode, fetchFlags, this);
    }

    @Override
    public void controllerReady(final UstadController controller, int flags) {
        if(controller == null) {
            //there was an error loading the controller
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            String errMsg = LocaleUtil.formatMessage(impl.getString(U.id.course_catalog_load_error),
                "Catalog controller");
            impl.getAppView().showAlertDialog(impl.getString(U.id.error), errMsg);
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentView.setController((CatalogController) controller);
                    setupFromCatalogView(currentView, mSavedInstanceState);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //figure out the current fragment
        Fragment catalogFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_CATALOG);

        CatalogViewAndroid viewToSave = catalogFragment != null ?
            (CatalogViewAndroid)((CatalogOPDSFragment)catalogFragment).getCatalogView() : currentView;

        outState.putString(EXTRA_CATALOGURL, viewToSave.getController().getModel().opdsFeed.href);
        outState.putInt(EXTRA_RESMODE, viewToSave.getController().getResourceMode());
        outState.putInt(UstadMobileSystemImplAndroid.EXTRA_VIEWID, viewToSave.getViewId());
    }

    public void setupFromCatalogView(CatalogViewAndroid view, Bundle savedInstanceState) {
        this.currentView = view;
        currentView.setCatalogViewActivity(this);
        CatalogController ctrl = currentView.getController();
        CatalogModel model = ctrl.getModel();
        UstadJSOPDSFeed feed = model.opdsFeed;
        setTitle(feed.title);
        setDrawerMenuItems(currentView.getMenuOptions());

        if(savedInstanceState == null) {
            CatalogOPDSFragment currentFrag = CatalogOPDSFragment.newInstance(viewId);
            getSupportFragmentManager().beginTransaction().add(R.id.catalog_fragment_container,
                    currentFrag, FRAGMENT_CATALOG).commit();
            mSavedInstanceState = null;
        }
    }

    public void setDrawerMenuItems(String[] drawerMenuItems) {
        this.drawerMenuItems = drawerMenuItems;
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawermenuitem,
                drawerMenuItems));
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

                fTransaction.replace(R.id.catalog_fragment_container, fragment,
                        CatalogActivity.FRAGMENT_CATALOG);
                fTransaction.addToBackStack(backEntryTitle);
                //fTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fTransaction.commit();

            }
        });

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentView.getController().handleClickMenuItem(position);
    }
}
