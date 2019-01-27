package com.ustadmobile.lib.db.entities;

public class ReportMasterItem  {

    private String clazzName;
    private String clazzId;
    private String firstName, lastName;
    private long personUid;
    private int daysPresent, daysAbsent, daysPartial, clazzDays;
    private long dateLeft;
    private boolean clazzMemberActive;
    private int gender;
    private long dateOfBirth;


     public String getClazzName() {
          return clazzName;
     }

     public void setClazzName(String clazzName) {
          this.clazzName = clazzName;
     }

     public String getClazzId() {
          return clazzId;
     }

     public void setClazzId(String clazzId) {
          this.clazzId = clazzId;
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

     public long getPersonUid() {
          return personUid;
     }

     public void setPersonUid(long personUid) {
          this.personUid = personUid;
     }

     public int getDaysPresent() {
          return daysPresent;
     }

     public void setDaysPresent(int daysPresent) {
          this.daysPresent = daysPresent;
     }

     public int getDaysAbsent() {
          return daysAbsent;
     }

     public void setDaysAbsent(int daysAbsent) {
          this.daysAbsent = daysAbsent;
     }

     public int getDaysPartial() {
          return daysPartial;
     }

     public void setDaysPartial(int daysPartial) {
          this.daysPartial = daysPartial;
     }

     public int getClazzDays() {
          return clazzDays;
     }

     public void setClazzDays(int clazzDays) {
          this.clazzDays = clazzDays;
     }

     public long getDateLeft() {
          return dateLeft;
     }

     public void setDateLeft(long dateLeft) {
          this.dateLeft = dateLeft;
     }

     public boolean isClazzMemberActive() {
          return clazzMemberActive;
     }

     public void setClazzMemberActive(boolean clazzMemberActive) {
          this.clazzMemberActive = clazzMemberActive;
     }

     public int getGender() {
          return gender;
     }

     public void setGender(int gender) {
          this.gender = gender;
     }

     public long getDateOfBirth() {
          return dateOfBirth;
     }

     public void setDateOfBirth(long dateOfBirth) {
          this.dateOfBirth = dateOfBirth;
     }
}
