package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.PasswordReset;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Person;

@DatabaseTable(tableName = "password_reset")
public class PasswordResetEntity implements PasswordReset {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_USER = "user";
	@DatabaseField(columnName = COLNAME_USER, foreign = true, foreignAutoRefresh = true)
	private PersonEntity user;
	static public final String COLNAME_REG_ID = "reg_id";
	@DatabaseField(columnName = COLNAME_REG_ID)
	private String regId;
	static public final String COLNAME_DONE = "done";
	@DatabaseField(columnName = COLNAME_DONE)
	private boolean done;
	static public final String COLNAME_DATE_ACCESSED = "date_accessed";
	@DatabaseField(columnName = COLNAME_DATE_ACCESSED)
	private long dateAccessed;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = (PersonEntity) user;
	}

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public long getDateAccessed() {
		return dateAccessed;
	}

	public void setDateAccessed(long dateAccessed) {
		this.dateAccessed = dateAccessed;
	}
}