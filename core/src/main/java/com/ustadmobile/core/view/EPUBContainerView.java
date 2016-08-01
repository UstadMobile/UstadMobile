/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.view;

/**
 *
 * @author mike
 */
public interface EPUBContainerView extends ContainerView {
    
    public void setEPUBTitle(String title);
    
    public String getEPUBTitle();
    
    public String[] getPageList();
    
    public void setPageList(String[] pageList);
    
    public void setCurrentPage(int pageNum);
    
    public int getCurrentPage();
    
}
