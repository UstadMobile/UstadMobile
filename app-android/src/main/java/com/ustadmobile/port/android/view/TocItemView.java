package com.ustadmobile.port.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.toughra.ustadmobile.R;

/**
 * View to hold items for the TocListView. This wraps the view provided by the adapter and puts an
 * expand/collapse arrow on the side if required.
 */
public class TocItemView extends LinearLayout implements View.OnClickListener {

    private FrameLayout itemViewLayout;

    private boolean expanded = false;

    private boolean expandable = false;

    private ImageView mDropDownImageView;

    public interface OnClickExpandListener {

        void onClickExpand(TocItemView itemView);

    }

    private OnClickExpandListener clickExpandListener;

    public TocItemView(Context context) {
        super(context);
        init();
    }

    public TocItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TocItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public TocItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.item_toclistview_itemcontainer, this);
        itemViewLayout = findViewById(R.id.item_toclistview_itemcontainer_frame_layout);
        mDropDownImageView = findViewById(R.id.item_toclistview_expand_img);
        mDropDownImageView.setOnClickListener(this);
    }

    /**
     * Set the view to be shown
     *
     * @param view
     */
    public void setItemView(View view) {
        itemViewLayout.removeAllViews();
        itemViewLayout.addView(view);
    }

    public void setOnClickExpandListener(OnClickExpandListener clickExpandListener) {
        this.clickExpandListener = clickExpandListener;
    }

    @Override
    public void onClick(View view) {
        if(clickExpandListener != null)
            clickExpandListener.onClickExpand(this);
    }

    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Set whether or not the node is expanded. This will only change the expand/collapse icon.
     *
     * @param expanded True to show that this item has been expanded (show up arrow), false otherwise
     *                 (down arrow)
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        mDropDownImageView.setImageResource(expanded ?
                R.drawable.ic_arrow_drop_up_black_24dp : R.drawable.ic_arrow_drop_down_black_24dp);
    }

    public boolean isExpandable() {
        return expandable;
    }

    /**
     * Set whether or not expand/collapse arrows should be shown on this view
     *
     * @param expandable true to show expand/collapse arrows, false otherwise
     */
    public void setExpandable(boolean expandable) {
        mDropDownImageView.setVisibility(expandable ? View.VISIBLE : View.INVISIBLE);
    }
}
