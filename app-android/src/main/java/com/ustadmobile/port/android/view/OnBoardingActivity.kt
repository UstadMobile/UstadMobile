package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.rd.PageIndicatorView
import com.rd.animation.type.AnimationType
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.OnBoardingPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.CompletableDeferred

class OnBoardingActivity : UstadBaseActivity(), OnBoardingView, AdapterView.OnItemClickListener {

    private var pageIndicatorView: PageIndicatorView? = null

    override var networkManager: CompletableDeferred<NetworkManagerBle>? = null

    private var presenter: OnBoardingPresenter? = null

    private lateinit var languageOptions: AutoCompleteTextView

    private lateinit var viewPager: ViewPager

    private var getStartedBtn: Button? = null

    private lateinit var screenList: List<OnBoardScreen>

    override val viewContext: Any
        get() = this

    /**
     * Model for the the onboarding screen
     */
    private enum class OnBoardScreen(val headlineStringResId: Int, val subHeadlineStringResId: Int,
                                     val layoutResId: Int, val drawableResId: Int) {
        SCREEN_1(R.string.onboarding_no_internet_headline,
                R.string.onboarding_no_internet_subheadline,
                R.layout.onboard_screen_view, R.drawable.downloading_data),
        SCREEN_2(R.string.onboarding_offline_sharing,
                R.string.onboarding_offline_sharing_subheading,
                R.layout.onboard_screen_view, R.drawable.sharing_data)
    }


    /**
     * Custom pager adapter for the screen
     */
    private inner class OnBoardingPagerAdapter internal constructor(private val context: Context) : PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val onBoardScreen = screenList[position]
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
            return screenList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

    }

    //We target lower than SDK 19, this check is a false flag when the devMinApi21Debug variant is selected
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        viewPager = findViewById(R.id.onBoardPagerView)
        getStartedBtn = findViewById(R.id.get_started_btn)
        pageIndicatorView = findViewById(R.id.pageIndicatorView)
        languageOptions = findViewById(R.id.language_options_autocomplete_textview)

        pageIndicatorView?.setAnimationType(AnimationType.WORM)
        getStartedBtn?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(resources.configuration.locale) == ViewCompat.LAYOUT_DIRECTION_RTL
        var firstScreenIndex = 0
        screenList = when (isRtl) {
            true -> {
                firstScreenIndex = OnBoardScreen.values().size - 1
                OnBoardScreen.values().reversed()
            }
            else -> OnBoardScreen.values().toList()
        }

        if (Build.VERSION.SDK_INT <= 19) {
            getStartedBtn?.setBackgroundResource(R.drawable.pre_lollipop_btn_selector_bg_onboarding)
            getStartedBtn?.setTextColor(ContextCompat.getColor(this,
                    R.color.pre_lollipop_btn_selector_txt_onboarding))
        }

        viewPager.adapter = OnBoardingPagerAdapter(this)
        viewPager.currentItem = firstScreenIndex


        if (pageIndicatorView != null) {
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float,
                                            positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    pageIndicatorView?.setSelected(position)

                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }



        presenter = OnBoardingPresenter(this,
                bundleToMap(intent.extras), this, UstadMobileSystemImpl.instance)
        presenter?.onCreate(bundleToMap(savedInstanceState))
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Handler().postDelayed(Runnable {
            presenter?.handleLanguageSelected(position)
        }, 5000)
    }

    override fun setLanguageOptions(languages: List<String>, currentSelection: String) {
        val adapter = ArrayAdapter(this, R.layout.autocomplete_list_item, languages)
        languageOptions.setAdapter(adapter)
        languageOptions.setText(currentSelection, false)
        languageOptions.onItemClickListener = this
    }

    override fun restartUI() {
        recreate()
    }

}