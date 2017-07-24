package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz;
import com.j256.ormlite.field.DatabaseField;
import java.util.Collection;
import com.ustadmobile.port.sharedse.persistence.proxy.School;

@DatabaseTable(tableName = "clazz")
public class ClazzEntity implements Clazz {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_NAME = "name";
	@DatabaseField(columnName = COLNAME_NAME)
	private String name;
	static public final String COLNAME_DESC = "desc";
	@DatabaseField(columnName = COLNAME_DESC)
	private String desc;
	static public final String COLNAME_LOCATION = "location";
	@DatabaseField(columnName = COLNAME_LOCATION)
	private String location;
	static public final String COLNAME_STUDENTS = "students";
	@DatabaseField(columnName = COLNAME_STUDENTS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity students;
	static public final String COLNAME_TEACHERS = "teachers";
	@DatabaseField(columnName = COLNAME_TEACHERS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity teachers;
	static public final String COLNAME_SCHOOL = "school";
	@DatabaseField(columnName = COLNAME_SCHOOL, foreign = true, foreignAutoRefresh = true)
	private SchoolEntity school;
	static public final String COLNAME_DAYS = "days";
	@DatabaseField(columnName = COLNAME_DAYS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity days;
	static public final String COLNAME_HOLIDAY_CALENDARS = "holiday_calendars";
	@DatabaseField(columnName = COLNAME_HOLIDAY_CALENDARS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity holidayCalendars;
	static public final String COLNAME_ALERTS = "alerts";
	@DatabaseField(columnName = COLNAME_ALERTS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity alerts;

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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Collection getStudents() {
		return students;
	}

	public void setStudents(Collection students) {
		this.students = (CollectionEntity) students;
	}

	public Collection getTeachers() {
		return teachers;
	}

	public void setTeachers(Collection teachers) {
		this.teachers = (CollectionEntity) teachers;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = (SchoolEntity) school;
	}

	public Collection getDays() {
		return days;
	}

	public void setDays(Collection days) {
		this.days = (CollectionEntity) days;
	}

	public Collection getHolidayCalendars() {
		return holidayCalendars;
	}

	public void setHolidayCalendars(Collection holidayCalendars) {
		this.holidayCalendars = (CollectionEntity) holidayCalendars;
	}

	public Collection getAlerts() {
		return alerts;
	}

	public void setAlerts(Collection alerts) {
		this.alerts = (CollectionEntity) alerts;
	}
}