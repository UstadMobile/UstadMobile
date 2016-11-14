package com.ustadmobile.port.android.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.model.ListableEntity;

import java.util.List;

/**
 * Created by mike on 14/11/16.
 */

public class EntityCardAdapter extends RecyclerView.Adapter<EntityCardAdapter.ViewHolder> implements View.OnClickListener{

    private List<? extends ListableEntity> mEntityList;

    private View.OnClickListener mOnClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public EntityCard mEntityCard;

        public ViewHolder(EntityCard entityCard) {
            super(entityCard);
            this.mEntityCard = entityCard;
        }
    }

    public EntityCardAdapter(List<? extends ListableEntity> entityList) {
        this.mEntityList = entityList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EntityCard entityCard = (EntityCard)LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entitycard, parent, false);
        ViewHolder vh = new ViewHolder(entityCard);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //set title here
        holder.mEntityCard.setEntity(mEntityList.get(position));
        holder.mEntityCard.setTitle(mEntityList.get(position).getTitle());
        holder.mEntityCard.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mEntityList.size();
    }

    @Override
    public void onClick(View view) {
        if(mOnClickListener != null) {
            mOnClickListener.onClick(view);
        }
    }

    public void setOnEntityClickListener(View.OnClickListener listener) {
        this.mOnClickListener = listener;
    }


}
