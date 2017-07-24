package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Enrollment;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz;
import com.ustadmobile.port.sharedse.persistence.proxy.Person;

@DatabaseTable(tableName = "enrollment")
public class EnrollmentEntity implements Enrollment {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_CLAZZ = "clazz";
	@DatabaseField(columnName = COLNAME_CLAZZ, foreign = true, foreignAutoRefresh = true)
	private ClazzEntity clazz;
	static public final String COLNAME_USER = "user";
	@DatabaseField(columnName = COLNAME_USER, foreign = true, foreignAutoRefresh = true)
	private PersonEntity user;
	static public final String COLNAME_DATE_JOINED = "date_joined";
	@DatabaseField(columnName = COLNAME_DATE_JOINED)
	private long dateJoined;
	static public final String COLNAME_DATE_MODIFIED = "date_modified";
	@DatabaseField(columnName = COLNAME_DATE_MODIFIED)
	private long dateModified;
	static public final String COLNAME_DATE_UPDATED = "date_updated";
	@DatabaseField(columnName = COLNAME_DATE_UPDATED)
	private long dateUpdated;
	static public final String COLNAME_ACTIVE = "active";
	@DatabaseField(columnName = COLNAME_ACTIVE)
	private boolean active;
	static public final String COLNAME_NOTES = "notes";
	@DatabaseField(columnName = COLNAME_NOTES)
	private String notes;
	static public final String COLNAME_ROLL_NUMBER = "roll_number";
	@DatabaseField(columnName = COLNAME_ROLL_NUMBER)
	private int rollNumber;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public Clazz getClazz() {
		return clazz;
	}

	public void setClazz(Clazz clazz) {
		this.clazz = (ClazzEntity) clazz;
	}

	public Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = (PersonEntity) user;
	}

	public long getDateJoined() {
		return dateJoined;
	}

	public void setDateJoined(long dateJoined) {
		this.dateJoined = dateJoined;
	}

	public long getDateModified() {
		return dateModified;
	}

	public void setDateModified(long dateModified) {
		this.dateModified = dateModified;
	}

	public long getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(long dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getRollNumber() {
		return rollNumber;
	}

	public void setRollNumber(int rollNumber) {
		this.rollNumber = rollNumber;
	}
}