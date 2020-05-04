package com.ustadmobile.port.android.umeditor

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem


/**
 * Interface which handles actions performed on ContentEditorPageListFragment
 *
 * @author kileha3
 */
interface UmPageActionListener {

    /**
     * Invoked when EpubNavItem list order changes
     * @param newPageList new EpubNavItem list order to be used
     */
    fun onOrderChanged(newPageList: List<EpubNavItem>)

    /**
     * Invoked when page deletion is confirmed.
     * @param href href of a page to be deleted
     */
    fun onPageRemove(href: String)

    /**
     * Invoked when page creation is confirmed
     * @param title title of the page to be created
     */
    fun onPageCreate(title: String)

    /**
     * Invoked when page to load is selected from the list
     * @param pageHref href of a page to be loaded to the editor
     */
    fun onPageSelected(pageHref: String)

    /**
     * Invoked when page update is confirmed
     * @param pageItem updated EpubNavItem
     */
    fun onPageUpdate(pageItem: EpubNavItem)

    /**
     * Invoked when document title change is confirmed
     * @param title new title to be used.
     */
    fun onDocumentUpdate(title: String, description: String)

    /**
     * Invoked when user tries to delete a page from a list of 1 page.
     * @param message message to be displayed.
     */
    fun onDeleteFailure(message: String)

    /**
     * invoked when the dialog is closed
     */
    fun onPageManagerClosed()

}
