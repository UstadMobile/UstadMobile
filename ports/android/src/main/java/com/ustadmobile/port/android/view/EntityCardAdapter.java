package com.ustadmobile.port.android.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.model.ListableEntity;

import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by mike on 14/11/16.
 */

public class EntityCardAdapter extends RecyclerView.Adapter<EntityCardAdapter.ViewHolder> implements View.OnClickListener{

    private List<? extends ListableEntity> mEntityList;

    private View.OnClickListener mOnClickListener;

    private WeakHashMap<String, EntityCard> idToCardMap;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public EntityCard mEntityCard;

        public ViewHolder(EntityCard entityCard) {
            super(entityCard);
            this.mEntityCard = entityCard;
        }
    }

    public EntityCardAdapter(List<? extends ListableEntity> entityList) {
        this.mEntityList = entityList;
        idToCardMap = new WeakHashMap<>();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EntityCard entityCard = (EntityCard)LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entitycard, parent, false);
        ViewHolder vh = new ViewHolder(entityCard);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //check if this is associated with another id
        Iterator<String> idIterator = idToCardMap.keySet().iterator();
        ListableEntity entity = mEntityList.get(position);

        String idVal;
        while(idIterator.hasNext()) {
            idVal = idIterator.next();
            if(idVal.equals(entity.getId()))
                idIterator.remove();
        }

        //set title here
        idToCardMap.put(entity.getId(), holder.mEntityCard);
        holder.mEntityCard.setEntity(entity);
        holder.mEntityCard.setTitle(mEntityList.get(position).getTitle());
        holder.mEntityCard.setStatusText(entity.getStatusText());
        holder.mEntityCard.setStatusIcon(entity.getStatusIconCode());
        holder.mEntityCard.setOnClickListener(this);
    }

    public EntityCard getCardByEntityId(String id) {
        return idToCardMap.get(id);
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
