package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.ustadmobile.core.controller.PersonWithSaleInfoDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.lib.db.entities.Person
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UMAndroidUtil

class PersonWithSaleInfoDetailActivity :UstadBaseActivity(), PersonWithSaleInfoDetailView,
        ViewPager.OnPageChangeListener {


     override fun onPageScrollStateChanged(state: Int) {}

     override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

     override fun onPageSelected(position: Int) {}

    private lateinit var mPresenter: PersonWithSaleInfoDetailPresenter


    private lateinit var mPager: ViewPager
    private var mPagerAdapter: PersonWithSaleInfoDetailViewPagerAdapter? = null

    private lateinit var saleListFragment: SaleListFragment
    private lateinit var personWithSaleInfoProfileFragment: PersonWithSaleInfoProfileFragment

    private var personUid = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_person_with_saleinfo_detail)
        val toolbar = findViewById<Toolbar>(R.id.activity_person_with_saleinfo_detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val args = UMAndroidUtil.bundleToMap(intent.extras)
        if(args.containsKey(PersonWithSaleInfoDetailView.ARG_WE_UID)){
            personUid = args.get(PersonWithSaleInfoDetailView.ARG_WE_UID)!!.toLong()
        }

        mPager = findViewById(R.id.activity_person_with_saleinfo_detail_viewpager)
        mPager.adapter = PersonWithSaleInfoDetailViewPagerAdapter(supportFragmentManager,
                this, personUid)
        val tabLayout = findViewById<TabLayout>(R.id.activity_person_with_saleinfo_detail_tabs)
        tabLayout.setupWithViewPager(mPager)
        mPager.addOnPageChangeListener(this)

        mPresenter = PersonWithSaleInfoDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun updatePersonOnView(person: Person) {
        if(person.firstNames != null && person.lastName != null){
            supportActionBar!!.setTitle(person.firstNames + " " + person.lastName)
        }
    }


    class PersonWithSaleInfoDetailViewPagerAdapter internal constructor(
            fragmentManager: FragmentManager, private val context: Context, private val weUid:Long)
        : FragmentPagerAdapter(fragmentManager) {

        private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

        // Returns total number of pages
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment? {
            val bundle = Bundle()

            return when (position) {
                0 // Fragment # 0 - This will show FirstFragment
                -> {
                    bundle.putString(PersonWithSaleInfoDetailView.ARG_WE_UID, weUid.toString())
                    PersonWithSaleInfoProfileFragment.newInstance(bundle)
                }
                1 // Fragment # 0 - This will show FirstFragment different title
                -> {
                    bundle.putString(PersonWithSaleInfoDetailView.ARG_WE_UID, weUid.toString())
                    SaleListFragment.newInstance(bundle)
                }
                else -> null
            }
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return impl.getString(MessageID.profile, context)
                1 -> return impl.getString(MessageID.sales, context)
            }
            return null

        }

        companion object {
            private const val NUM_ITEMS = 2
        }

    }



}