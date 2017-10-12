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
package com.ustadmobile.core.view;

import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;

import java.util.Vector;

/**
 *
 * @author varuna
 */
public interface CatalogView extends UstadView{
    
    public int CMD_DOWNLOADALL = 0;
    
    public int CMD_DELETEITEM = 1;
    
    public int CMD_DOWNLOADENTRY = 2;

    public static final String VIEW_NAME = "Catalog";


    /**
     * Set the status of a given entry (e.g. acquired, not acquired, in progress) 
     * 
     * @param entryId Id of the entry to update
     * @param status Entry status flag ( CatalogController.STATUS_ACQUIRED, STATUS_ACQUISITION_IN_PROGRESS, or STATUS_NOT_ACQUIRED)
     */
    public void setEntryStatus(String entryId, int status);
    
    /**
     * Set the thumbnail for the given entry.  Loading the thumbnails is threaded
     * and this method should handle putting calls onto the UI thread as required
     * 
     * @param entryId the entry id for the entry to s
     * @param iconFileURI A file with an icon image
     */
    public void setEntrythumbnail(String entryId, String iconFileURI);

    /**
     * Set the background for the given catalog entry.  This is not found in standard OPDS
     * catalogs and only effective when enabled using AppConfig.OPDS_ITEM_ENABLE_BACKGROUNDS = true
     *
     * @param entryId The entry ID for the image to set background on
     * @param backgroundFileURI URI of a file downloaded (in a cache directory) to use as the background
     */
    void setEntryBackground(String entryId, String backgroundFileURI);

    /**
     * Set the background to use for the catalog itself
     * 
     * @param backgroundFileURI
     */
    void setCatalogBackground(String backgroundFileURI);

    /**
     * Update the download all progress bar at the bottom
     * 
     * @param loaded amount loaded (e.g. in total bytes downloaded) 
     * @param total total length total length of download, or -1 if not yet known
     */
    public void updateDownloadAllProgress(int loaded, int total);
    
    /**
     * Set whether a progress bar is visible on a given catalog (acquisition) entry
     * @param entryId Entry to update
     * @param visible true for visible, false otherwise
     */
    public void setDownloadEntryProgressVisible(String entryId, boolean visible);
    
    /**
     * Update the progress bar showing the progress on a given download entry
     * 
     * @param entryId The entry id to be updated
     * @param progress The progress as a float between 0 and 1
     * @param statusText The status text to display to the user
     */
    void updateDownloadEntryProgress(String entryId, float progress, String statusText);

    /**
     * Show the user's progress on this course (displayed to the right of the entry as a donut
     * chart (e.g. fitness goal style).
     *
     * @param entryId
     * @param progress
     */
    void setEntryProgress(String entryId, CourseProgress progress);

    
    /**
     * Get the entries that have been selected by the user (e.g. by long press)
     * 
     * @return Array of entries selected by the user
     */
    Vector getSelectedEntries();
    
    /**
     * Set the entries that are to be marked as selected
     * 
     * @param entries Array of entries to be marked as selected
     */
    void setSelectedEntries(Vector entries);

    public void refresh();

    /**
     * Sets whether or not to show a browse button leading to another catalog etc. on this view.
     * This would normally be fixed at the bottom; not part of any scrolling
     *
     * @param buttonVisible true to make it visible; false otherwise
     */
    public void setFooterButtonVisible(boolean buttonVisible);

    /**
     * Sets the label for the browse button
     *
     * @param browseButtonLabel Text label for the browse button
     */
    public void setFooterButtonLabel(String browseButtonLabel);

    public void setDeleteOptionAvailable(boolean deleteOptionAvailable);

    public void setAddOptionAvailable(boolean addOptionAvailable);

    /**
     * Set alternative (translated) versions of this catalog, as per the rel='alternate' hreflang='other-lang'
     * links. The view should then call CatalogController.handleClickAlternativeTranslationLink
     * with the index of the language selected
     *
     * @param translationLinks String array of other languages.
     * @param disabledItem The index of an item which should be disabled. This would normally be the
     *                     language the catalog is already in (if known). Providing a value < 0
     *                     means none of the items are to be disabled.
     */
    void setAlternativeTranslationLinks(String[] translationLinks, int disabledItem);

    /**
     * Add an entry to the catalog display list
     *
     * @param entry
     */
    void addEntry(UstadJSOPDSEntry entry);

    /**
     * Add an entry to the catalog display list at the specified index
     *
     * @param position
     * @param entry
     */
    void addEntry(int position, UstadJSOPDSEntry entry);

    void setEntryAt(int position, UstadJSOPDSEntry entry);

    /**
     * Remove an entry from the catalog display list
     *
     * @param entry
     */
    void removeEntry(UstadJSOPDSEntry entry);

    void removeEntry(int index);

    /**
     * Return the number of entries in this view
     *
     * @return
     */
    int getNumEntries();

    /**
     * Get the index of a given entry id
     *
     * @param entryId
     * @return
     */
    int indexOfEntry(String entryId);

    void setRefreshing(boolean isRefreshing);


}
