package com.ustadmobile.lib.db.entities;

public class SaleNameWithImage {

    private String name;
    private String description;
    private long pictureUid;
    private int type;
    private long productUid;
    private long productGroupUid;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPictureUid() {
        return pictureUid;
    }

    public void setPictureUid(long pictureUid) {
        this.pictureUid = pictureUid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getProductUid() {
        return productUid;
    }

    public void setProductUid(long productUid) {
        this.productUid = productUid;
    }

    public long getProductGroupUid() {
        return productGroupUid;
    }

    public void setProductGroupUid(long productGroupUid) {
        this.productGroupUid = productGroupUid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
