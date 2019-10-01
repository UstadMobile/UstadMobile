import { Component, OnInit, NgZone, ViewChild, ElementRef } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import core from 'UstadMobile-core'
import { Subscription, Subject } from 'rxjs';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { UmAngularUtil } from '../../util/UmAngularUtil';
@Component({
  selector: 'app-h5p-content',
  templateUrl: './h5p-content.component.html',
  styleUrls: ['./h5p-content.component.css']
})
export class H5pContentComponent extends UmBaseComponent implements core.com.ustadmobile.core.view.H5PContentView {

  private presenter: core.com.ustadmobile.core.controller.H5PContentPresenter
  private navigationSubscription: Subscription;
  contentToLoad: SafeHtml = ""

  constructor(umservice: UmBaseService, router: Router, route: ActivatedRoute, public sanitizer: DomSanitizer, private zone:NgZone) { 
    super(umservice,router, route)

     //Listen for the navigation changes - changes on url
     this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
     .subscribe(_ => {
       UmAngularUtil.registerResourceReadyListener(this)
     });
  }

  ngOnInit() {
    super.ngOnInit()
  }

  onCreate(){
    super.onCreate()
    this.presenter = new core.com.ustadmobile.core.controller.H5PContentPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this, this.containerMounter)
    this.presenter.onCreate(null)
  }


  containerMounter(containerUid){
    return UmAngularUtil.getMountPath(containerUid)
  }

  setContentTitle(title){

  }

  setContentHtml(url, html){
    this.zone.run(() => {
      this.contentToLoad = this.sanitizer.bypassSecurityTrustHtml(html)
    })
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
