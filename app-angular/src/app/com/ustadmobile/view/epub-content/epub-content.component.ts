import { Component, NgZone } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import core from 'UstadMobile-core';
import { Subscription } from 'rxjs';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import {  DomSanitizer } from '@angular/platform-browser';
 
@Component({
  selector: 'app-epub-content',
  templateUrl: './epub-content.component.html',
  styleUrls: ['./epub-content.component.css']
})
export class EpubContentComponent extends UmBaseComponent implements core.com.ustadmobile.core.view.EpubContentView{

  private presenter: core.com.ustadmobile.core.controller.EpubContentPresenter;
  private navigationSubscription: Subscription;
  urlsToLoad = []
  urlToLoad = ""
  currentIndex = 0;
  private inMemoryUrls = [] 

  options = { fullWidth: false };

  constructor(umservice: UmBaseService, router: Router, route: ActivatedRoute, public sanitizer: DomSanitizer, private zone:NgZone) {
    super(umservice,router, route)

    //Listen for the navigation changes - changes on url
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
      });
  }

  onCreate(){
    super.onCreate()
    this.presenter = new core.com.ustadmobile.core.controller.EpubContentPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this)
    this.presenter.onCreate(null)
  }

  ngOnInit() {
    super.ngOnInit()
    $(document).ready(function() {
      //$('iframe').contents().find('img').addClass("responsive-img")
  });
  }


  setContainerTitle(title){}

  setSpineUrls(urls){
    this.inMemoryUrls = urls;
  }

  setPageTitle(title){}

  setTableOfContents(epubNav){}

  setCoverImage(url){
    let allUrls = []
    allUrls.push(url)
    this.urlsToLoad = allUrls.concat(this.inMemoryUrls)
    this.loadPage(0)
  }

  private loadPage(index){
    this.zone.run( ()=>{
      this.currentIndex = index
      this.urlToLoad = this.urlsToLoad[this.currentIndex]
    })
  }

  setAuthorName(name){}

  mountContainer(containerUid, callback){
    callback.onSuccess(localStorage.getItem(this.umService.baseUrlTag)+"ContainerMount/"+containerUid+"/")
  }

  unmountContainer(mountUrl){}

  setProgressBarVisible(progress){}

  setProgressBarProgress(progress){}

  goToLinearSpinePosition(position){}

  goToNextPage(){
    let nextIndex = this.currentIndex + 1
    nextIndex = nextIndex >=  this.urlsToLoad.length ? nextIndex = this.urlsToLoad.length - 1: nextIndex
    this.loadPage(nextIndex)
  }

  getToPrevPage(){
    let prevIndex = this.currentIndex - 1
    prevIndex = prevIndex <= 0 ? 0: prevIndex
    this.loadPage(prevIndex)
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    if (this.presenter) {
      this.presenter.onDestroy();
    }
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

}
