package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Entity that represents content as it is browsed by the user. A ContentEntry can be either:
 *  1. An actual piece of content (e.g. book, course, etc), in which case there should be an associated
 *     ContentEntryFile.
 *  2. A navigation directory (e.g. a category as it is scraped from another site, etc), in which case
 *     there should be the appropriate ContentEntryParentChildJoin entities present.
 */
@UmEntity
public class ContentEntry {

    public static final int LICENSE_TYPE_CC_BY = 1;

    public static final int LICENSE_TYPE_CC_BY_SA = 2;

    public static final int LICENSE_TYPE_CC_BY_SA_NC = 3;

    @UmPrimaryKey(autoIncrement = true)
    private long contentEntryUid;

    private String title;

    private String description;

    private int primaryLanguage;

    private String entryId;

    private String author;

    private String publisher;

    private int licenseType;

    private String licenseName;

    private String licenseUrl;

    private String sourceUrl;

    public long getContentEntryUid() {
        return contentEntryUid;
    }

    public void setContentEntryUid(long contentEntryUid) {
        this.contentEntryUid = contentEntryUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(int primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(int licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
