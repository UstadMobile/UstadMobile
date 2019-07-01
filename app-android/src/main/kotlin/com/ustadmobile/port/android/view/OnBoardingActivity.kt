package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.rd.PageIndicatorView
import com.rd.animation.type.AnimationType
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.OnBoardingPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.sharedse.network.NetworkManagerBle

class OnBoardingActivity : UstadBaseActivity(), OnBoardingView {

    private var pageIndicatorView: PageIndicatorView? = null

    private var presenter: OnBoardingPresenter? = null

    private var viewPager: ViewPager? = null

    private var getStartedBtn: Button? = null

    override val viewContext: Any
        get() = this

    /**
     * Model for the the onboarding screen
     */
    private enum class OnBoardScreen(val headlineStringResId: Int, val subHeadlineStringResId: Int,
                                     val layoutResId: Int, val drawableResId: Int)


    /**
     * Custom pager adapter for the screen
     */
    private inner class OnBoardingPagerAdapter internal constructor(private val context: Context) : PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val onBoardScreen = OnBoardScreen.values()[position]
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(onBoardScreen.layoutResId,
                    collection, false) as ViewGroup
            (layout.findViewById<View>(R.id.heading) as TextView).text = context.getString(onBoardScreen.headlineStringResId)
            (layout.findViewById<View>(R.id.sub_heading) as TextView).text = context.getString(onBoardScreen.subHeadlineStringResId)
            (layout.findViewById<View>(R.id.drawable_res) as ImageView)
                    .setImageResource(onBoardScreen.drawableResId)
            collection.addView(layout)
            return layout
        }

        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return OnBoardScreen.values().size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        viewPager = findViewById(R.id.onBoardPagerView)
        getStartedBtn = findViewById(R.id.get_started_btn)
        pageIndicatorView = findViewById(R.id.pageIndicatorView)

        presenter = OnBoardingPresenter(this,
                bundleToMap(intent.extras), this)
        presenter!!.onCreate(bundleToMap(savedInstanceState))
        pageIndicatorView!!.setAnimationType(AnimationType.WORM)

        getStartedBtn!!.setOnClickListener { v -> presenter!!.handleGetStarted() }

    }

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        if (networkManagerBle.isVersionKitKatOrBelow) {
            getStartedBtn!!.setBackgroundResource(R.drawable.pre_lollipop_btn_selector_bg_onboarding)
            getStartedBtn!!.setTextColor(resources
                    .getColorStateList(R.color.pre_lollipop_btn_selector_txt_onboarding))
        }
    }

    override fun setScreenList() {
        viewPager!!.adapter = OnBoardingPagerAdapter(this)
        if (pageIndicatorView != null) {
            viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float,
                                            positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    pageIndicatorView!!.setSelected(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }
}
