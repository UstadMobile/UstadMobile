package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.OnBoardingPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.CompletableDeferred
import org.kodein.di.direct
import org.kodein.di.instance

class OnBoardingActivity : UstadBaseActivity(), OnBoardingView, AdapterView.OnItemClickListener {

    private var presenter: OnBoardingPresenter? = null

    private lateinit var languageOptions: AutoCompleteTextView

    private lateinit var viewPager: ViewPager2

    private var getStartedBtn: Button? = null

    private lateinit var screenList: List<OnBoardScreen>

    //Do nothing - there isn't really any loading of this
    override var loading: Boolean = false

    /**
     * Model for the the onboarding screen
     */
    private enum class OnBoardScreen(val headlineStringResId: Int, val subHeadlineStringResId: Int,
                                     val layoutResId: Int, val drawableResId: Int) {
        SCREEN_1(R.string.onboarding_no_internet_headline,
                R.string.onboarding_no_internet_subheadline,
                R.layout.onboard_screen_view, R.drawable.illustration_offline_usage),
        SCREEN_2(R.string.onboarding_offline_sharing,
                R.string.onboarding_offline_sharing_subheading,
                R.layout.onboard_screen_view, R.drawable.illustration_offline_sharing),
        SCREEN_3(R.string.onboarding_stay_organized_headline,
                R.string.onboarding_stay_organized_subheading,
                R.layout.onboard_screen_view, R.drawable.illustration_organized)
    }


    /**
     * Custom pager adapter for the screen
     */
    private inner class OnBoardingPagerAdapter constructor(private val context: Context)
        : RecyclerView.Adapter<OnBoardingPagerAdapter.BoardScreenHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardScreenHolder {
            return BoardScreenHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
        }

        override fun getItemViewType(position: Int): Int {
            return screenList[position].layoutResId
        }

        override fun getItemCount(): Int {
            return screenList.size
        }

        override fun onBindViewHolder(holder: BoardScreenHolder, position: Int) {
            holder.bind(screenList[position])
        }

        inner class BoardScreenHolder internal constructor(val view: View) :
                RecyclerView.ViewHolder(view) {

            internal fun bind(screen: OnBoardScreen) {
                (view.findViewById<View>(R.id.heading) as TextView).text = context.getString(screen.headlineStringResId)
                (view.findViewById<View>(R.id.sub_heading) as TextView).text = context.getString(screen.subHeadlineStringResId)
                (view.findViewById<View>(R.id.drawable_res) as ImageView)
                        .setImageResource(screen.drawableResId)
            }

        }
    }

    //We target lower than SDK 19, this check is a false flag when the devMinApi21Debug variant is selected
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        viewPager = findViewById(R.id.onBoardPagerView)
        getStartedBtn = findViewById(R.id.get_started_btn)
        languageOptions = findViewById(R.id.language_options_autocomplete_textview)


        //We have to put this here because there is no VIEW_NAME for MainActivity. This will
        // have to be resolved by RedirectFragment
        getStartedBtn?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val systemImpl: UstadMobileSystemImpl = di.direct.instance()
            systemImpl.setAppPref(OnBoardingView.PREF_TAG, true.toString(), this)

            startActivity(intent)
        }

        screenList =  OnBoardScreen.values().toList()

        if (Build.VERSION.SDK_INT <= 19) {
            getStartedBtn?.setBackgroundResource(R.drawable.pre_lollipop_btn_selector_bg_onboarding)
            getStartedBtn?.setTextColor(ContextCompat.getColor(this,
                    R.color.pre_lollipop_btn_selector_txt_onboarding))
        }

        viewPager.adapter = OnBoardingPagerAdapter(this)


        presenter = OnBoardingPresenter(this,
                bundleToMap(intent.extras), this, di)
        presenter?.onCreate(bundleToMap(savedInstanceState))
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        presenter?.handleLanguageSelected(position)
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