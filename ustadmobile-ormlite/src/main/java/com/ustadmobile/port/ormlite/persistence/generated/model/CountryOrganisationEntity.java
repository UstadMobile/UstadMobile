package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.CountryOrganisation;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Organisation;

@DatabaseTable(tableName = "country_organisation")
public class CountryOrganisationEntity implements CountryOrganisation {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_COUNTRY_CODE = "country_code";
	@DatabaseField(columnName = COLNAME_COUNTRY_CODE)
	private String countryCode;
	static public final String COLNAME_ORGANISATION = "organisation";
	@DatabaseField(columnName = COLNAME_ORGANISATION, foreign = true, foreignAutoRefresh = true)
	private OrganisationEntity organisation;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = (OrganisationEntity) organisation;
	}
}