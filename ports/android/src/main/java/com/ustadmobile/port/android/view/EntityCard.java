package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.model.ListableEntity;

/**
 * EntityCard : Generic Reusable Card to show an entity with an icon, status, title and detail text
 */

public class EntityCard extends CardView {

    //Icon IDs that match with AttendanceController.STATUS_
    private static final int[] STATUS_ID_DRAWABLES = {
            R.drawable.ic_priority_high_black_18dp,
            R.drawable.ic_sync_black_18dp,
            R.drawable.ic_done_black_18dp
    };

    private ListableEntity entity;


    public EntityCard(Context ctx) {
        super(ctx);
    }

    public EntityCard(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public EntityCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setEntity(ListableEntity entity) {
        this.entity = entity;
    }

    public ListableEntity getEntity() {
        return this.entity;
    }

    public void setTitle(String title) {
        ((TextView)findViewById(R.id.entity_item_title_text)).setText(title);
    }

    public void setStatusText(String statusText) {
        ((TextView)findViewById(R.id.entity_item_status_text)).setText(statusText);
    }

    public void setStatusIcon(int iconId) {
        ((ImageView)findViewById(R.id.entity_item_status_icon)).setImageDrawable(
                ContextCompat.getDrawable(getContext(), STATUS_ID_DRAWABLES[iconId]));
    }



}
