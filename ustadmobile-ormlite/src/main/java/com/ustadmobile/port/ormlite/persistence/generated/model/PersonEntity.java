package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Person;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Organisation;
import com.ustadmobile.port.sharedse.persistence.proxy.Role;

@DatabaseTable(tableName = "person")
public class PersonEntity implements Person {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_FIRST_NAME = "first_name";
	@DatabaseField(columnName = COLNAME_FIRST_NAME)
	private String firstName;
	static public final String COLNAME_LAST_NAME = "last_name";
	@DatabaseField(columnName = COLNAME_LAST_NAME)
	private String lastName;
	static public final String COLNAME_LAST_LOGIN = "last_login";
	@DatabaseField(columnName = COLNAME_LAST_LOGIN)
	private long lastLogin;
	static public final String COLNAME_SUPERUSER = "superuser";
	@DatabaseField(columnName = COLNAME_SUPERUSER)
	private boolean superuser;
	static public final String COLNAME_EMAIL = "email";
	@DatabaseField(columnName = COLNAME_EMAIL)
	private String email;
	static public final String COLNAME_STAFF = "staff";
	@DatabaseField(columnName = COLNAME_STAFF)
	private boolean staff;
	static public final String COLNAME_ACTIVE = "active";
	@DatabaseField(columnName = COLNAME_ACTIVE)
	private boolean active;
	static public final String COLNAME_DATE_JOINED = "date_joined";
	@DatabaseField(columnName = COLNAME_DATE_JOINED)
	private long dateJoined;
	static public final String COLNAME_WEBSITE = "website";
	@DatabaseField(columnName = COLNAME_WEBSITE)
	private String website;
	static public final String COLNAME_COMPANY_NAME = "company_name";
	@DatabaseField(columnName = COLNAME_COMPANY_NAME)
	private String companyName;
	static public final String COLNAME_JOB_TITLE = "job_title";
	@DatabaseField(columnName = COLNAME_JOB_TITLE)
	private String jobTitle;
	static public final String COLNAME_DATE_OF_BIRTH = "date_of_birth";
	@DatabaseField(columnName = COLNAME_DATE_OF_BIRTH)
	private long dateOfBirth;
	static public final String COLNAME_ADDRESS = "address";
	@DatabaseField(columnName = COLNAME_ADDRESS)
	private String address;
	static public final String COLNAME_PHONE_NUMBER = "phone_number";
	@DatabaseField(columnName = COLNAME_PHONE_NUMBER)
	private String phoneNumber;
	static public final String COLNAME_GENDER = "gender";
	@DatabaseField(columnName = COLNAME_GENDER)
	private String gender;
	static public final String COLNAME_ADMIN_APPROVED = "admin_approved";
	@DatabaseField(columnName = COLNAME_ADMIN_APPROVED)
	private boolean adminApproved;
	static public final String COLNAME_ORGANISATION_REQUESTED = "organisation_requested";
	@DatabaseField(columnName = COLNAME_ORGANISATION_REQUESTED, foreign = true, foreignAutoRefresh = true)
	private OrganisationEntity organisationRequested;
	static public final String COLNAME_NOTES = "notes";
	@DatabaseField(columnName = COLNAME_NOTES)
	private String notes;
	static public final String COLNAME_LAST_ACTIVITY_DATE = "last_activity_date";
	@DatabaseField(columnName = COLNAME_LAST_ACTIVITY_DATE)
	private long lastActivityDate;
	static public final String COLNAME_CUSTOM_ROLL_NUMBER = "custom_roll_number";
	@DatabaseField(columnName = COLNAME_CUSTOM_ROLL_NUMBER)
	private String customRollNumber;
	static public final String COLNAME_ORGANISATION = "organisation";
	@DatabaseField(columnName = COLNAME_ORGANISATION, foreign = true, foreignAutoRefresh = true)
	private OrganisationEntity organisation;
	static public final String COLNAME_ROLE = "role";
	@DatabaseField(columnName = COLNAME_ROLE, foreign = true, foreignAutoRefresh = true)
	private RoleEntity role;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public long getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}

	public boolean isSuperuser() {
		return superuser;
	}

	public void setSuperuser(boolean superuser) {
		this.superuser = superuser;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isStaff() {
		return staff;
	}

	public void setStaff(boolean staff) {
		this.staff = staff;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getDateJoined() {
		return dateJoined;
	}

	public void setDateJoined(long dateJoined) {
		this.dateJoined = dateJoined;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public long getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(long dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public boolean isAdminApproved() {
		return adminApproved;
	}

	public void setAdminApproved(boolean adminApproved) {
		this.adminApproved = adminApproved;
	}

	public Organisation getOrganisationRequested() {
		return organisationRequested;
	}

	public void setOrganisationRequested(Organisation organisationRequested) {
		this.organisationRequested = (OrganisationEntity) organisationRequested;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public long getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(long lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	public String getCustomRollNumber() {
		return customRollNumber;
	}

	public void setCustomRollNumber(String customRollNumber) {
		this.customRollNumber = customRollNumber;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = (OrganisationEntity) organisation;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = (RoleEntity) role;
	}
}