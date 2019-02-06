package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.VideoPlayerPresenter;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

public class VideoPlayerActivity extends UstadBaseActivity implements VideoPlayerView {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private VideoPlayerPresenter mPresenter;
    boolean isPortrait = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            isPortrait = true;
            setContentView(R.layout.activity_portrait_video_player_view);
        } else {
            isPortrait = false;
            setContentView(R.layout.activity_landscape_video_player_view);
        }


        playerView = findViewById(R.id.activity_video_player_view);

        if (isPortrait) {
            setUMToolbar(R.id.activity_video_player_toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPresenter = new VideoPlayerPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

        playerView.setPlayer(player);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);

        Uri uri = Uri.parse(mPresenter.getVideoPath());
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, false, false);


    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        if (!isPortrait) {
            playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

    }

    @Override
    public void loadUrl(String firstUrl) {


    }

    @Override
    public void setVideoInfo(ContentEntry result) {
        runOnUiThread(() -> {
            if (isPortrait) {
                ((Toolbar)findViewById(R.id.activity_video_player_toolbar)).setTitle(result.getTitle());
                ((TextView)findViewById(R.id.activity_video_player_description)).setText(result.getDescription());
            }
        });
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new FileDataSourceFactory())
                .createMediaSource(uri);
    }
}
