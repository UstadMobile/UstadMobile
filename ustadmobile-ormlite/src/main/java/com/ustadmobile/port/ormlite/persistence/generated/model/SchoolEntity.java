package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.School;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Organisation;
import java.util.Collection;

@DatabaseTable(tableName = "school")
public class SchoolEntity implements School {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_NAME = "name";
	@DatabaseField(columnName = COLNAME_NAME)
	private String name;
	static public final String COLNAME_DESC = "desc";
	@DatabaseField(columnName = COLNAME_DESC)
	private String desc;
	static public final String COLNAME_ORGANISATION = "organisation";
	@DatabaseField(columnName = COLNAME_ORGANISATION, foreign = true, foreignAutoRefresh = true)
	private OrganisationEntity organisation;
	static public final String COLNAME_LONGITUDE = "longitude";
	@DatabaseField(columnName = COLNAME_LONGITUDE)
	private long longitude;
	static public final String COLNAME_LATTITUDE = "lattitude";
	@DatabaseField(columnName = COLNAME_LATTITUDE)
	private long lattitude;
	static public final String COLNAME_SHOW_LOCATION = "show_location";
	@DatabaseField(columnName = COLNAME_SHOW_LOCATION)
	private boolean showLocation;
	static public final String COLNAME_SHOW_EXACT_LOCATION = "show_exact_location";
	@DatabaseField(columnName = COLNAME_SHOW_EXACT_LOCATION)
	private boolean showExactLocation;
	static public final String COLNAME_WEEKENDS = "weekends";
	@DatabaseField(columnName = COLNAME_WEEKENDS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity weekends;
	static public final String COLNAME_HOLIDAY_CALENDAR = "holiday_calendar";
	@DatabaseField(columnName = COLNAME_HOLIDAY_CALENDAR, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity holidayCalendar;
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

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = (OrganisationEntity) organisation;
	}

	public long getLongitude() {
		return longitude;
	}

	public void setLongitude(long longitude) {
		this.longitude = longitude;
	}

	public long getLattitude() {
		return lattitude;
	}

	public void setLattitude(long lattitude) {
		this.lattitude = lattitude;
	}

	public boolean isShowLocation() {
		return showLocation;
	}

	public void setShowLocation(boolean showLocation) {
		this.showLocation = showLocation;
	}

	public boolean isShowExactLocation() {
		return showExactLocation;
	}

	public void setShowExactLocation(boolean showExactLocation) {
		this.showExactLocation = showExactLocation;
	}

	public Collection getWeekends() {
		return weekends;
	}

	public void setWeekends(Collection weekends) {
		this.weekends = (CollectionEntity) weekends;
	}

	public Collection getHolidayCalendar() {
		return holidayCalendar;
	}

	public void setHolidayCalendar(Collection holidayCalendar) {
		this.holidayCalendar = (CollectionEntity) holidayCalendar;
	}

	public Collection getAlerts() {
		return alerts;
	}

	public void setAlerts(Collection alerts) {
		this.alerts = (CollectionEntity) alerts;
	}
}