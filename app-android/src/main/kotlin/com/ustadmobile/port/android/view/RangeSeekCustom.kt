package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet

import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar

class RangeSeekCustom : CrystalRangeSeekbar {
    private val THUMB_SIZE = 40

    protected val barHeight: Float
        get() = 5f

    protected val thumbWidth: Float
        get() = THUMB_SIZE.toFloat()

    protected val thumbHeight: Float
        get() = THUMB_SIZE.toFloat()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
}
