package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.OrgAlertSettings;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Organisation;

@DatabaseTable(tableName = "org_alert_settings")
public class OrgAlertSettingsEntity implements OrgAlertSettings {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_ORGANISATION = "organisation";
	@DatabaseField(columnName = COLNAME_ORGANISATION, foreign = true, foreignAutoRefresh = true)
	private OrganisationEntity organisation;
	static public final String COLNAME_CUT_OFF_TIME = "cut_off_time";
	@DatabaseField(columnName = COLNAME_CUT_OFF_TIME)
	private int cutOffTime;

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

	public int getCutOffTime() {
		return cutOffTime;
	}

	public void setCutOffTime(int cutOffTime) {
		this.cutOffTime = cutOffTime;
	}
}