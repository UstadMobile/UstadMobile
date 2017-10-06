package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.ClassManagementController2;
import com.ustadmobile.port.sharedse.view.ClassManagementView2;

import java.util.Hashtable;
import java.util.WeakHashMap;

public class ClassManagementActivity2 extends UstadBaseActivity implements ClassManagementView2 {

    private ClassManagementController2 mController;

    private Hashtable baseArgs;

    public static final Class[] TAB_CLASSES = new Class[]{PersonListFragment.class, AttendanceListFragment.class};

    private ClassManagementPagerAdapter2 mPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_management2);
        baseArgs = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        mController = ClassManagementController2.makeControllerForView(baseArgs, this);
        mController.setView(this);
        setBaseController(mController);
        setUMToolbar(R.id.um_toolbar);

        mPagerAdapter = new ClassManagementPagerAdapter2(getSupportFragmentManager());
        mViewPager = (ViewPager)findViewById(R.id.activity_class_management_pager);
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.activity_class_management_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mController.setUIStrings();
    }

    @Override
    public void setClassName(String className) {
        setTitle(className);
    }

    public class ClassManagementPagerAdapter2 extends FragmentStatePagerAdapter {

        private int[] tabTitles = new int[]{MessageID.students, MessageID.attendance};

        private WeakHashMap<Integer, Fragment> fragmentMap;

        public ClassManagementPagerAdapter2(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return UstadBaseFragment.newInstance(ClassManagementActivity2.this.baseArgs, TAB_CLASSES[position]);
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return UstadMobileSystemImpl.getInstance().getString(tabTitles[position],
                    ClassManagementActivity2.this);
        }
    }
}
