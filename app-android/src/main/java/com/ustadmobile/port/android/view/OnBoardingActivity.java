package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.OnBoardingPresenter;
import com.ustadmobile.core.view.OnBoardingView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class OnBoardingActivity extends AppCompatActivity implements OnBoardingView {

    private PageIndicatorView pageIndicatorView;

    private OnBoardingPresenter presenter;

    private enum OnBoardScreen {

        SCREEN_1(R.string.about,R.string.about,R.layout.onboard_screen_view),
        SCREEN_2(R.string.about,R.string.about,R.layout.onboard_screen_view),
        SCREEN_3(R.string.about,R.string.about,R.layout.onboard_screen_view);

        private int headlineStringResId;

        private int subHeadlineStringResId;

        private int layoutResId;

        OnBoardScreen(int headlineStringResId, int subHeadlineStringResId,int layoutResId){
            this.headlineStringResId= headlineStringResId;
            this.subHeadlineStringResId = subHeadlineStringResId;
            this.layoutResId = layoutResId;
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
    }


    private class OnBoardingPagerAdapter extends PagerAdapter{

        private Context context;

        OnBoardingPagerAdapter(Context context){
            this.context = context;
        }

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
        ViewPager viewPager = findViewById(R.id.onBoardPagerView);
        Button getStartedBtn = findViewById(R.id.get_started_btn);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);

        presenter = new OnBoardingPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),this);
        pageIndicatorView.setAnimationType(AnimationType.WORM);

        getStartedBtn.setOnClickListener(v -> presenter.handleGetStarted());

        viewPager.setAdapter(new OnBoardingPagerAdapter(this));

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

    @Override
    public Object getContext() {
        return this;
    }

    @Override
    public int getDirection() {
        return 0;
    }

    @Override
    public void setDirection(int dir) {

    }

    @Override
    public void setAppMenuCommands(String[] labels, int[] ids) {

    }

    @Override
    public void setUIStrings() {

    }
}
