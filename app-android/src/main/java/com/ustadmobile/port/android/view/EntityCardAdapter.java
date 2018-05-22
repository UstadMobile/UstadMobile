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

    private boolean statusVisible;

    private boolean detailTextVisible;

    private int entityIconId;

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
        statusVisible = true;
        entityIconId = R.drawable.ic_group_white_24dp;
    }

    public boolean isStatusVisible() {
        return statusVisible;
    }

    public void setStatusVisible(boolean statusVisible) {
        this.statusVisible = statusVisible;
        Iterator<EntityCard> iterator = idToCardMap.values().iterator();
        while(iterator.hasNext()) {
            iterator.next().setStatusVisible(statusVisible);
        }
    }

    public boolean isDetailTextVisible() {
        return detailTextVisible;
    }

    public void setDetailTextVisible(boolean detailTextVisible) {
        this.detailTextVisible = detailTextVisible;
        Iterator<EntityCard> iterator = idToCardMap.values().iterator();
        while(iterator.hasNext()) {
            iterator.next().setDetailTextVisible(detailTextVisible);
        }
    }

    public int getEntityIconId() {
        return entityIconId;
    }

    public void setEntityIconId(int entityIconId) {
        this.entityIconId = entityIconId;
        Iterator<EntityCard> iterator = idToCardMap.values().iterator();
        while(iterator.hasNext()) {
            iterator.next().setEntityIcon(entityIconId);
        }
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
        holder.mEntityCard.setStatusText(entity.getStatusText(holder.mEntityCard.getContext()));
        holder.mEntityCard.setStatusIcon(entity.getStatusIconCode());
        holder.mEntityCard.setDetailTextVisible(detailTextVisible);
        holder.mEntityCard.setDetailText(entity.getDetail());
        holder.mEntityCard.setStatusVisible(statusVisible);
        holder.mEntityCard.setEntityIcon(entityIconId);
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
