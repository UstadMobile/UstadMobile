package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz2UMCalendar;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz;
import com.ustadmobile.port.sharedse.persistence.proxy.UMCalendar;

@DatabaseTable(tableName = "clazz2umcalendar")
public class Clazz2UMCalendarEntity implements Clazz2UMCalendar {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_CLAZZ = "clazz";
	@DatabaseField(columnName = COLNAME_CLAZZ, foreign = true, foreignAutoRefresh = true)
	private ClazzEntity clazz;
	static public final String COLNAME_HOLIDAY = "holiday";
	@DatabaseField(columnName = COLNAME_HOLIDAY, foreign = true, foreignAutoRefresh = true)
	private UMCalendarEntity holiday;

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

	public UMCalendar getHoliday() {
		return holiday;
	}

	public void setHoliday(UMCalendar holiday) {
		this.holiday = (UMCalendarEntity) holiday;
	}
}