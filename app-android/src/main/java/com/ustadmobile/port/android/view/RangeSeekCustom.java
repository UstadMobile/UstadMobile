package com.ustadmobile.port.android.view;

import android.content.Context;
import android.util.AttributeSet;

import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;

public class RangeSeekCustom extends CrystalRangeSeekbar {
    private final int THUMB_SIZE = 40;
    public RangeSeekCustom(Context context) {
        super(context);
    }

    public RangeSeekCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RangeSeekCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected float getBarHeight() {
        return 5;
    }

    @Override
    protected float getThumbWidth() {
        return THUMB_SIZE;
    }

    @Override
    protected float getThumbHeight() {
        return THUMB_SIZE;
    }
}
