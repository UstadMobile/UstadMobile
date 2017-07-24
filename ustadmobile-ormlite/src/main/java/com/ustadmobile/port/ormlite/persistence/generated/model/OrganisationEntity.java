package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.AttendanceStatusLabel;
import com.ustadmobile.port.sharedse.persistence.proxy.Organisation;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.UMCalendar;
import com.ustadmobile.port.sharedse.persistence.proxy.UMPackage;

@DatabaseTable(tableName = "organisation")
public class OrganisationEntity implements Organisation {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_NAME = "name";
	@DatabaseField(columnName = COLNAME_NAME)
	private String name;
	static public final String COLNAME_DESC = "desc";
	@DatabaseField(columnName = COLNAME_DESC)
	private String desc;
	static public final String COLNAME_PUBLIK = "publik";
	@DatabaseField(columnName = COLNAME_PUBLIK)
	private boolean publik;
	static public final String COLNAME_CALENDAR = "calendar";
	@DatabaseField(columnName = COLNAME_CALENDAR, foreign = true, foreignAutoRefresh = true)
	private UMCalendarEntity calendar;
	static public final String COLNAME_U_MPACKAGE = "u_mpackage";
	@DatabaseField(columnName = COLNAME_U_MPACKAGE, foreign = true, foreignAutoRefresh = true)
	private UMPackageEntity uMPackage;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String getCode() {
		return null;
	}

	@Override
	public void setCode(String code) {

	}

	@Override
	public AttendanceStatusLabel getStatusLabel() {
		return null;
	}

	@Override
	public void setStatusLabel(AttendanceStatusLabel statusLabel) {

	}

	public boolean isPublik() {
		return publik;
	}

	public void setPublik(boolean publik) {
		this.publik = publik;
	}

	public UMCalendar getCalendar() {
		return calendar;
	}

	public void setCalendar(UMCalendar calendar) {
		this.calendar = (UMCalendarEntity) calendar;
	}

	public UMPackage getUMPackage() {
		return uMPackage;
	}

	public void setUMPackage(UMPackage uMPackage) {
		this.uMPackage = (UMPackageEntity) uMPackage;
	}
}