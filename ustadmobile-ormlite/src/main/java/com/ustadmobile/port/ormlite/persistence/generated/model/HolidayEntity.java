package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Holiday;
import com.j256.ormlite.field.DatabaseField;
import java.util.Collection;

@DatabaseTable(tableName = "holiday")
public class HolidayEntity implements Holiday {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_NAME = "name";
	@DatabaseField(columnName = COLNAME_NAME)
	private String name;
	static public final String COLNAME_DATE = "date";
	@DatabaseField(columnName = COLNAME_DATE)
	private long date;
	static public final String COLNAME_CALENDARS = "calendars";
	@DatabaseField(columnName = COLNAME_CALENDARS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity calendars;

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

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public Collection getCalendars() {
		return calendars;
	}

	public void setCalendars(Collection calendars) {
		this.calendars = (CollectionEntity) calendars;
	}
}