/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.impl;

/**
 *
 * @author mike
 */
public class UMStorageDir {
    
    private String dirURI;

    private boolean removableMedia = false;

    private String name;

    private boolean available;
    
    private boolean userSpecific;
    
    public UMStorageDir(String dirURI, String name, boolean removableMedia, boolean available, boolean userSpecific) {
        this.dirURI = dirURI;
        this.name = name;
        this.removableMedia = removableMedia;
        this.available = available;
        this.userSpecific = userSpecific;
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer(dirURI);
        sb.append(dirURI).append(" (").append(name).append(" )");
        sb.append(" available: ").append(available);
        sb.append(" removable: ").append(removableMedia);
        sb.append(" user specific: ").append(userSpecific);
        return sb.toString();
    }
    
    /**
     * Whether this directory is only for the given logged in user 
     * or the directory is shared for all users on this device
     *
     * @return true if this directory is only for the current user, false otherwise
     */
    public boolean isUserSpecific() {
        return userSpecific;
    }

    /**
     * Set whether or not this directory is user specific
     *
     * @param userSpecific true if this directory is only for the current user, false otherwise
     */
    public void setUserSpecific(boolean userSpecific) {
        this.userSpecific = userSpecific;
    }


    
    /**
     * Whether or not this storage location is currently available for use
     *
     * @return the value of available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Set the value of available
     *
     * @param available new value of available
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * The user friendly name e.g. SD-Card or Phone
     *
     * @return the user friendly name
     */
    public String getName() {
        return name;
    }

    /**
     * The user friendly name e.g. SD-Card or Phone
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
    }

    
    /**
     * Get the value of isRemovableMedia
     *
     * @return the value of isRemovableMedia
     */
    public boolean isRemovableMedia() {
        return removableMedia;
    }

    /**
     * Set the value of isRemovableMedia
     *
     * @param removableMedia new value of isRemovableMedia
     */
    public void setRemovableMedia(boolean removableMedia) {
        this.removableMedia = removableMedia;
    }

    /**
     * Get the value of dirURI
     *
     * @return the value of dirURI
     */
    public String getDirURI() {
        return dirURI;
    }

    /**
     * Set the value of dirURI
     *
     * @param dirURI new value of dirURI
     */
    public void setDirURI(String dirURI) {
        this.dirURI = dirURI;
    }

}
