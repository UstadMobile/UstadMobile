package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.School2Weekend;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.School;
import com.ustadmobile.port.sharedse.persistence.proxy.Day;

@DatabaseTable(tableName = "school2weekend")
public class School2WeekendEntity implements School2Weekend {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_SCHOOL = "school";
	@DatabaseField(columnName = COLNAME_SCHOOL, foreign = true, foreignAutoRefresh = true)
	private SchoolEntity school;
	static public final String COLNAME_DAY = "day";
	@DatabaseField(columnName = COLNAME_DAY, foreign = true, foreignAutoRefresh = true)
	private DayEntity day;

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

	public Day getDay() {
		return day;
	}

	public void setDay(Day day) {
		this.day = (DayEntity) day;
	}
}