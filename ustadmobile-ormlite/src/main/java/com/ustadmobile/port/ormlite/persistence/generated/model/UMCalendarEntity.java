package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.UMCalendar;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Organisation;
import java.util.Collection;

@DatabaseTable(tableName = "u_mcalendar")
public class UMCalendarEntity implements UMCalendar {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_ORGANISATION = "organisation";
	@DatabaseField(columnName = COLNAME_ORGANISATION, foreign = true, foreignAutoRefresh = true)
	private OrganisationEntity organisation;
	static public final String COLNAME_NAME = "name";
	@DatabaseField(columnName = COLNAME_NAME)
	private String name;
	static public final String COLNAME_HOLIDAYS = "holidays";
	@DatabaseField(columnName = COLNAME_HOLIDAYS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity holidays;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection getHolidays() {
		return holidays;
	}

	public void setHolidays(Collection holidays) {
		this.holidays = (CollectionEntity) holidays;
	}
}