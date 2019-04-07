package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.OnBoardingPresenter;
import com.ustadmobile.core.view.OnBoardingView;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

public class OnBoardingActivity extends UstadBaseActivity implements OnBoardingView {

    private PageIndicatorView pageIndicatorView;

    private OnBoardingPresenter presenter;

    private  ViewPager viewPager;

    private Button getStartedBtn;

    /**
     * Model for the the onboarding screen
     */
    private enum OnBoardScreen {


        SCREEN_1(R.string.onboarding_goldozi_1,R.string.onboarding_goldozi_1b
                ,R.layout.onboard_screen_view, R.drawable.goldozi_logo1),
        SCREEN_2(R.string.onboarding_goldozi_2,R.string.onboarding_empty,
                R.layout.onboard_screen_view, R.drawable.goldozi_products1),
        SCREEN_3(R.string.onboarding_goldozi_3,R.string.onboarding_empty,
                R.layout.onboard_screen_view, R.drawable.goldozi_sales1),
        SCREEN_4(R.string.onboarding_goldozi_4,R.string.onboarding_empty,
                R.layout.onboard_screen_view, R.drawable.goldozi_courses1);



        private int headlineStringResId;

        private int subHeadlineStringResId;

        private int layoutResId;

        private int drawableResId;

        OnBoardScreen(int headlineStringResId, int subHeadlineStringResId,
                      int layoutResId, int drawableResId){
            this.headlineStringResId= headlineStringResId;
            this.subHeadlineStringResId = subHeadlineStringResId;
            this.layoutResId = layoutResId;
            this.drawableResId = drawableResId;
        }

        public int getHeadlineStringResId() {
            return headlineStringResId;
        }

        public int getSubHeadlineStringResId() {
            return subHeadlineStringResId;
        }

        public int getLayoutResId() {
            return layoutResId;
        }

        public int getDrawableResId() {
            return drawableResId;
        }
    }


    /**
     * Custom pager adapter for the screen
     */
    private class OnBoardingPagerAdapter extends PagerAdapter{

        private Context context;

        OnBoardingPagerAdapter(Context context){
            this.context = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            OnBoardScreen onBoardScreen = OnBoardScreen.values()[position];
            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(onBoardScreen.getLayoutResId(),
                    collection, false);
            ((TextView)layout.findViewById(R.id.heading))
                    .setText(context.getString(onBoardScreen.getHeadlineStringResId()));
            ((TextView)layout.findViewById(R.id.sub_heading))
                    .setText(context.getString(onBoardScreen.getSubHeadlineStringResId()));
            ((ImageView)layout.findViewById(R.id.drawable_res))
                    .setImageResource(onBoardScreen.getDrawableResId());
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return OnBoardScreen.values().length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        viewPager = findViewById(R.id.onBoardPagerView);
        getStartedBtn = findViewById(R.id.get_started_btn);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);

        presenter = new OnBoardingPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
        pageIndicatorView.setAnimationType(AnimationType.WORM);

        getStartedBtn.setOnClickListener(v -> presenter.handleGetStarted());

    }

    @Override
    protected void onBleNetworkServiceBound(NetworkManagerBle networkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle);
        if(networkManagerBle != null && networkManagerBle.isVersionKitKatOrBelow()){
            getStartedBtn.setBackgroundResource(R.drawable.pre_lollipop_btn_selector_bg_onboarding);
            getStartedBtn.setTextColor(getResources()
                    .getColorStateList(R.color.pre_lollipop_btn_selector_txt_onboarding));
        }
    }

    @Override
    public void setScreenList() {
        viewPager.setAdapter(new OnBoardingPagerAdapter(this));
        if(pageIndicatorView != null){
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) { }

                @Override
                public void onPageSelected(int position) {
                    pageIndicatorView.setSelected(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
        }
    }

    @Override
    public Object getContext() {
        return this;
    }
}
