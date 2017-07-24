package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Alert;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Organisation;
import java.util.Collection;

@DatabaseTable(tableName = "alert")
public class AlertEntity implements Alert {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_ORGANISATION = "organisation";
	@DatabaseField(columnName = COLNAME_ORGANISATION, foreign = true, foreignAutoRefresh = true)
	private OrganisationEntity organisation;
	static public final String COLNAME_SCHOOLS = "schools";
	@DatabaseField(columnName = COLNAME_SCHOOLS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity schools;
	static public final String COLNAME_CLAZZES = "clazzes";
	@DatabaseField(columnName = COLNAME_CLAZZES, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity clazzes;
	static public final String COLNAME_DAY_OF_WEEK = "day_of_week";
	@DatabaseField(columnName = COLNAME_DAY_OF_WEEK)
	private int dayOfWeek;
	static public final String COLNAME_MONTH = "month";
	@DatabaseField(columnName = COLNAME_MONTH)
	private int month;
	static public final String COLNAME_DAY_OF_MONTH = "day_of_month";
	@DatabaseField(columnName = COLNAME_DAY_OF_MONTH)
	private int dayOfMonth;
	static public final String COLNAME_MINUTE = "minute";
	@DatabaseField(columnName = COLNAME_MINUTE)
	private int minute;
	static public final String COLNAME_ACTIVE = "active";
	@DatabaseField(columnName = COLNAME_ACTIVE)
	private boolean active;
	static public final String COLNAME_ALERT_TYPES = "alert_types";
	@DatabaseField(columnName = COLNAME_ALERT_TYPES, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity alertTypes;
	static public final String COLNAME_TO_EMAILS = "to_emails";
	@DatabaseField(columnName = COLNAME_TO_EMAILS)
	private String toEmails;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = (OrganisationEntity) organisation;
	}

	public Collection getSchools() {
		return schools;
	}

	public void setSchools(Collection schools) {
		this.schools = (CollectionEntity) schools;
	}

	public Collection getClazzes() {
		return clazzes;
	}

	public void setClazzes(Collection clazzes) {
		this.clazzes = (CollectionEntity) clazzes;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Collection getAlertTypes() {
		return alertTypes;
	}

	public void setAlertTypes(Collection alertTypes) {
		this.alertTypes = (CollectionEntity) alertTypes;
	}

	public String getToEmails() {
		return toEmails;
	}

	public void setToEmails(String toEmails) {
		this.toEmails = toEmails;
	}
}