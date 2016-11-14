package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.model.ListableEntity;

/**
 * EntityCard : Generic Reusable Card to show an entity with an icon, status, title and detail text
 */

public class EntityCard extends CardView {

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




}
