package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.School2UMCalendar;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.School;
import com.ustadmobile.port.sharedse.persistence.proxy.UMCalendar;

@DatabaseTable(tableName = "school2umcalendar")
public class School2UMCalendarEntity implements School2UMCalendar {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_SCHOOL = "school";
	@DatabaseField(columnName = COLNAME_SCHOOL, foreign = true, foreignAutoRefresh = true)
	private SchoolEntity school;
	static public final String COLNAME_CALENDAR = "calendar";
	@DatabaseField(columnName = COLNAME_CALENDAR, foreign = true, foreignAutoRefresh = true)
	private UMCalendarEntity calendar;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = (SchoolEntity) school;
	}

	public UMCalendar getCalendar() {
		return calendar;
	}

	public void setCalendar(UMCalendar calendar) {
		this.calendar = (UMCalendarEntity) calendar;
	}
}